package daos

import javax.inject.{Inject, Singleton}
import models.{CSVQuotesQuery, FavQuoteQuery, Genre}
import models.Genre.Genre
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile
import slick.lifted.{ForeignKeyQuery, ProvenShape}

import scala.concurrent.{ExecutionContext, Future}

/**
  * A repository for Quotes stored in quotations table.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  * We want the JdbcProfile for this provider
  */
@Singleton
class CSVQuotesQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(
    implicit executionContext: ExecutionContext
) {

  lazy val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  // Slick mapping custom type enum in database column
  implicit val genreEnumMapper: BaseTypedType[Genre.Value] =
    MappedColumnType.base[Genre, String]({ genre =>
      genre.toString
    }, { string =>
      Genre.withName(string)
    })

  /**
    * Here we define the table. It will have a quotations
    */
  private class CSVQuoteQueriesTable(tag: Tag) extends Table[CSVQuotesQuery](tag, "quotations") {
    def id: Rep[Int]        = column[Int]("id", O.AutoInc)
    def csvid: Rep[String]  = column[String]("csvid", O.PrimaryKey)
    def quote: Rep[String]  = column[String]("quote")
    def author: Rep[String] = column[String]("author")
    def genre: Rep[Genre]   = column[Genre]("genre")

    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the QuotesQuery object.
      *
      * In this case, we are simply passing the id, quote, author and genre parameters to the QuotesQuery case classes
      * apply and unapply methods.
      */
    def * : ProvenShape[CSVQuotesQuery] =
      (id, csvid, quote, author, genre) <> ((CSVQuotesQuery.apply _).tupled, CSVQuotesQuery.unapply)

    def favQuote: ForeignKeyQuery[FavQuoteQueriesTable, FavQuoteQuery] =
      foreignKey("fav_quotations", csvid, favQuoteQueries)(_.csvid)
  }

  private class FavQuoteQueriesTable(tag: Tag) extends Table[FavQuoteQuery](tag, "fav_quotations") {
    def id: Rep[Int]         = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def csvid: Rep[String]   = column[String]("csvid")
    def favTag: Rep[Boolean] = column[Boolean]("favtag")
    def * : ProvenShape[FavQuoteQuery] =
      (id, csvid, favTag) <> ((FavQuoteQuery.apply _).tupled, FavQuoteQuery.unapply)
  }

  /**
    * The starting point for all queries on the quotations table.
    */
  private val CSVQuoteQueries = TableQuery[CSVQuoteQueriesTable]

  private val favQuoteQueries = TableQuery[FavQuoteQueriesTable]

  /**
    * @return list of custom quotes
    */
  def listAllQuotes(): Future[Seq[CSVQuotesQuery]] =
    db.run(CSVQuoteQueries.result)

  /**
    * @return quotes that are marked as favorite
    */
  def listAllFavQuotes(): Future[Seq[CSVQuotesQuery]] = {

    val query = CSVQuoteQueries
      .join(favQuoteQueries.filter(_.favTag))
      .on(_.csvid === _.csvid)

    db.run(query.result).map(_.map(_._1))
  }

  /**
    * @param genre to filter records with that genre
    * @return lists of quotes that matches input genre
    */
  def getGenreQuote(genre: Genre): Future[Option[CSVQuotesQuery]] = {
    val randomFunction = SimpleFunction.nullary[Double]("random")
    db.run(
      CSVQuoteQueries
        .filter(_.genre === genre)
        .sortBy(x => randomFunction)
        .result
        .headOption
    )
  }
}
