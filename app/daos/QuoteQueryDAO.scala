package daos

import javax.inject.Inject
import models.QuotesQuery
import models.Genre.Genre
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import table.QuoteQueriesTable
import utils.DbRunner
import utils.Implicits._

/**
  * A repository for Quotes stored in quotations table.
  */
class QuoteQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)
    extends CommonMethods
    with DbRunner {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  type T = QuotesQuery

  /**
    * @return List of all stored quotes
    */
  def listAllQuotes(): Seq[QuotesQuery] =
    runDbAction(QuoteQueriesTable.quoteQueries.result)

  /**
    * @param records number of records to return
    * @return Random quote from the database table
    */
  override def listRandomQuote(records: Int): Seq[QuotesQuery] =
    runDbAction(
      QuoteQueriesTable.quoteQueries
        .sortBy(_ => randomFunction)
        .take(records)
        .result
    )

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
