package daos

import javax.inject.Inject
import models.CSVQuotesQuery
import models.Genre.Genre
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import table.{ CSVQuoteQueriesTable, FavQuoteQueriesTable }
import utils.DbRunner
import utils.Implicits._

/**
  * A repository for Quotes stored in quotations table.
  */
class CSVQuotesQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider) extends DbRunner {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]
  private lazy val randomFunction                    = SimpleFunction.nullary[Double]("rand")

  /**
    * @return list of all stored quotes
    */
  def listAllQuotes(): Seq[CSVQuotesQuery] =
    runDbAction(CSVQuoteQueriesTable.CSVQuoteQueries.result)

  def randomQuote(): Option[CSVQuotesQuery] =
    runDbAction(
      CSVQuoteQueriesTable.CSVQuoteQueries
        .sortBy(_ => randomFunction)
        .take(1)
        .result
        .headOption
    )

  /**
    * @return quotes that are marked as favorite
    */
  def listAllFavQuotes(): Seq[CSVQuotesQuery] = {
    val query = CSVQuoteQueriesTable.CSVQuoteQueries
      .join(FavQuoteQueriesTable.favQuoteQueries.filter(_.favTag))
      .on(_.csvid === _.csvid)

    runDbAction(query.result).map(_._1)
    //.map(_.map(_._1))
  }

  /**
    * @param genre to filter records with that genre
    * @return lists of quotes that matches input genre
    */
  def getGenreQuote(genre: Genre): Option[CSVQuotesQuery] = {
    runDbAction(
      CSVQuoteQueriesTable.CSVQuoteQueries
        .filter(_.genre === genre)
        .sortBy(_ => randomFunction)
        .result
        .headOption
    )
  }
}
