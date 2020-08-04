package daos

import forms.CustomQuoteForm
import javax.inject.{ Inject, Singleton }
import models.CustomQuotesQuery
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import table.CustomQuotesQueriesTable
import utils.Implicits.genreEnumMapper
import utils.{ DbRunner, Logging }

/**
  * A repository for the custom quotes
  *
  * This class has a `Singleton` annotation because we need to make
  * sure we only use one CustomQuotesQueryDAO per application. Without this
  * annotation we would get a new instance every time a [[CustomQuoteQueryDAO]] is
  * injected.
  */

@Singleton
class CustomQuoteQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)
    extends DbRunner
    with Logging {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  /**
    * List all the records from the table
    * @return sequence of the CustomQuotesQuery records
    */
  def listCustomQuotes(): Seq[CustomQuotesQuery] =
    runDbAction(CustomQuotesQueriesTable.customQuoteQueries.sortBy(_.id).result)

  /**
    * List the JSON format of the selected record from the table
    * @param id quote id
    * @return Option of the CustomQuotesQuery record
    */
  def listSelectedQuote(id: Int): Option[CustomQuotesQuery] = {
    runDbAction(CustomQuotesQueriesTable.customQuoteQueries.filter(_.id === id).result.headOption)
  }

  /**
    * Defined custom function for slick 3
    * aware that "random" function is database specific
    * @return Option of CustomQuotesQuery
    */
  def listRandomQuote(): Option[CustomQuotesQuery] = {
    val randomFunction = SimpleFunction.nullary[Double]("random")
    runDbAction(
      CustomQuotesQueriesTable.customQuoteQueries.sortBy(_ => randomFunction).result.headOption
    )
  }

  /**
    * Create a customQuotes in the table.
    * This is an asynchronous operation, it will return a future of the created customQuotes,
    * which can be used to obtain the id for that person.
    */
  def createQuote(customQuote: CustomQuoteForm): CustomQuotesQuery = {
    val currentDate = new java.sql.Date(System.currentTimeMillis())
    val insertQuery = CustomQuotesQueriesTable.customQuoteQueries returning
      CustomQuotesQueriesTable.customQuoteQueries.map(_.id) into (
        (
            fields,
            id
        ) => fields.copy(id = id)
    )
    val action = insertQuery += CustomQuotesQuery(
      0,
      customQuote.quote,
      customQuote.author,
      customQuote.genre,
      currentDate,
      customQuote.ownquote
    )
    runDbAction(action)
  }

  /**
    * @param id quote record id
    * @param customQuote updated custom quote object
    * @return number of updated records, just 1 here
    */
  def updateQuote(id: Int, customQuote: CustomQuoteForm): Int = {
    runDbAction(
      CustomQuotesQueriesTable.customQuoteQueries
        .filter(_.id === id)
        .map(quote => (quote.quote, quote.author, quote.genre, quote.ownquote))
        .update(
          customQuote.quote,
          customQuote.author,
          customQuote.genre,
          customQuote.ownquote
        )
    )
  }

  /**
    * Delete the record from the table
    * @param id of the selected row from the CustomQuotesQuery table
    */
  def deleteQuote(id: Int): Unit = {
    runDbAction(CustomQuotesQueriesTable.customQuoteQueries.filter(_.id === id).delete)
  }
}
