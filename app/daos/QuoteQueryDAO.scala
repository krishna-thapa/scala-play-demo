package daos

import com.krishna.util.DbRunner
import javax.inject.Inject
import models.QuotesQuery
import models.Genre.Genre
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import table.QuoteQueriesTable
import utils.Implicits._

/**
  * A repository for Quotes stored in quotations table.
  */
class QuoteQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)
    extends CommonMethods[QuotesQuery]
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

  /**
    * @param parameter input author string to get the autocomplete
    * @return List of top 10 authors that matches on search string
    */
  def searchAuthors(parameter: String): Seq[String] = {
    // Left space is ignored and right space is used to search for next word
    val inputParam: String = parameter.replaceAll("^\\s+", "").toLowerCase
    runDbAction(
      QuoteQueriesTable.quoteQueries
        .filter(_.author.toLowerCase like s"%$inputParam%")
        .result
    ).map(_.author)
      .distinct
      .sortBy(!_.startsWith(inputParam.take(1).capitalize)) //Sorted by the first letter of parameter
      .take(10)
  }
}
