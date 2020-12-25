package daos

import com.krishna.model.Genre.Genre
import com.krishna.model.QuotesQuery
import com.krishna.services.RepositoryMethods
import com.krishna.util.DbRunner
import com.krishna.util.Implicits._
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import tables.QuoteQueriesTable

import javax.inject.Inject

/**
  * A repository for Quotes stored in quotes table.
  */
class QuoteQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)
    extends RepositoryMethods[QuotesQuery, QuoteQueriesTable]
    with DbRunner {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  override def tables: TableQuery[QuoteQueriesTable] = QuoteQueriesTable.quoteQueries

  /**
    * @return List of all stored quotes from database
    */
  def listAllQuotes: Seq[QuotesQuery] =
    runDbAction(getAllQuotes)

  /**
    * @param records number of records to return
    * @return Random quote from the database table
    */
  def listRandomQuote(records: Int): Seq[QuotesQuery] = {
    runDbAction(getRandomRecords(records))
  }

  /**
    * @param genre to filter records with that genre
    * @return Random quote that matches input genre
    */
  def listGenreQuote(genre: Genre): Option[QuotesQuery] = {
    runDbAction(
      tables
        .filter(_.genre === genre)
        .sortBy(_ => randomFunction)
        .result
        .headOption
    )
  }

  /**
    * First get the distinct on authors and then use like command to filter out
    * @param parameter input author string to get the autocomplete
    * @return List of top 10 authors that matches on search string
    */
  def searchAuthors(parameter: String): Seq[String] = {
    // Left space is ignored and right space is used to search for next word
    val inputParam: String = parameter.replaceAll("^\\s+", "").toLowerCase
    runDbAction(
      QuoteQueriesTable.quoteQueries
        .distinctOn(_.author)
        .filter(_.author.toLowerCase like s"%$inputParam%")
        .result
    ).map(_.author)
      .sortBy(!_.startsWith(inputParam.take(1).capitalize)) //Sorted by the first letter of parameter
      .take(10)
  }
}
