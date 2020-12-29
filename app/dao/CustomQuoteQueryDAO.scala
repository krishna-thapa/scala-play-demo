package dao

import com.krishna.services.RepositoryUserMethods
import com.krishna.util.Implicits.genreEnumMapper
import com.krishna.util.{ DbRunner, Logging }
import forms.CustomQuoteForm
import models.CustomQuotesQuery
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{ JdbcBackend, JdbcProfile }
import table.CustomQuotesQueriesTable

import javax.inject.{ Inject, Singleton }
import scala.util.Try

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
    extends RepositoryUserMethods[CustomQuotesQuery, CustomQuotesQueriesTable]
    with DbRunner
    with Logging {

  override val dbConfig: JdbcBackend#DatabaseDef = dbConfigProvider.get[JdbcProfile].db

  override def tables: TableQuery[CustomQuotesQueriesTable] =
    CustomQuotesQueriesTable.customQuoteQueries

  /**
    * List all the records from the table
    * @param userId: Logged in user id
    * @return sequence of the CustomQuotesQuery records
    */
  def listAllQuotes(userId: Int): Seq[CustomQuotesQuery] =
    runDbAction(getAllQuotesForUser(userId))

  /**
    * List the JSON format of the selected record from the table
    * @param id quote id
    * @return Option of the CustomQuotesQuery record
    */
  def listSelectedQuote(id: Int): Option[CustomQuotesQuery] = {
    runDbAction(getSelectedQuote(id))
  }

  /**
    * Defined custom function for slick 3
    * aware that "random" function is database specific
    * @return Option of CustomQuotesQuery
    */
  def listRandomQuote(records: Int): Seq[CustomQuotesQuery] = {
    runDbAction(getRandomRecords(records))
  }

  /**
    * Create a customQuotes in the table.
    * This is an asynchronous operation, it will return a future of the created customQuotes,
    * which can be used to obtain the id for that person.
    */
  def createQuote(customQuoteForm: CustomQuoteForm): CustomQuotesQuery = {
    val currentDate = new java.sql.Date(System.currentTimeMillis())
    val insertQuery = tables returning
      tables.map(_.id) into (
        (
            fields,
            id
        ) => fields.copy(id = id)
    )
    val action = insertQuery += CustomQuotesQuery(
      0,
      1,
      customQuoteForm.quote,
      customQuoteForm.author,
      customQuoteForm.genre,
      currentDate,
      customQuoteForm.ownQuote
    )
    runDbAction(action)
  }

  /**
    * @param id quote record id
    * @param customQuoteForm updated custom quote object
    * @return number of updated records, just 1 here
    */
  def updateQuote(id: Int, customQuoteForm: CustomQuoteForm): Try[Int] = {
    runDbActionCatchError(
      tables
        .filter(_.id === id)
        .map(quote => (quote.quote, quote.author, quote.genre, quote.ownQuote))
        .update(
          customQuoteForm.quote,
          customQuoteForm.author,
          customQuoteForm.genre,
          customQuoteForm.ownQuote
        )
    )
  }

  /**
    * Delete the record from the table
    * @param id of the selected row from the CustomQuotesQuery table
    */
  def deleteQuote(id: Int): Int = {
    runDbAction(deleteCustomQuote(id))
  }

}
