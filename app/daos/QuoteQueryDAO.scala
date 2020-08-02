package daos

import javax.inject.Inject
import models.QuotesQuery
import models.Genre.Genre
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import table.{ QuoteQueriesTable, FavQuoteQueriesTable }
import utils.DbRunner
import utils.Implicits._

/**
  * A repository for Quotes stored in quotations table.
  */
class QuoteQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider) extends DbRunner {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]
  private lazy val randomFunction                    = SimpleFunction.nullary[Double]("random")

  /**
    * @return List of all stored quotes
    */
  def allQuotes(): Seq[QuotesQuery] =
    runDbAction(QuoteQueriesTable.quoteQueries.result)

  /**
    * @param records number of records to return
    * @return Random quote from the database table
    */
  def randomQuote(records: Int): Seq[QuotesQuery] =
    runDbAction(
      QuoteQueriesTable.quoteQueries
        .sortBy(_ => randomFunction)
        .take(records)
        .result
    )

  /**
    * @return Quotes that are marked as favorite
    */
  def listAllFavQuotes(): Seq[QuotesQuery] = {
    val query = QuoteQueriesTable.quoteQueries
      .join(FavQuoteQueriesTable.favQuoteQueries.filter(_.favTag))
      .on(_.csvid === _.csvid)

    runDbAction(query.result).map(_._1)
  }

  /**
    * @param genre to filter records with that genre
    * @return Random quote that matches input genre
    */
  def getGenreQuote(genre: Genre): Option[QuotesQuery] = {
    runDbAction(
      QuoteQueriesTable.quoteQueries
        .filter(_.genre === genre)
        .sortBy(_ => randomFunction)
        .result
        .headOption
    )
  }
}
