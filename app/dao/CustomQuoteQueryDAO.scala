package dao

import com.krishna.services.RepositoryUserMethods
import com.krishna.util.Implicits.genreEnumMapper
import com.krishna.util.{ DbRunner, Logging }
import forms.CustomQuoteForm
import model.UserDetail
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
    * List the JSON format of the selected record from the table for logged in user
    * @param id quote id
    * @param userId logged in user id
    * @return Option of the CustomQuotesQuery record
    */
  def listSelectedQuote(id: Int, userId: Int): Option[CustomQuotesQuery] = {
    runDbAction(getSelectedQuote(id, userId))
  }

  /**
    * Return a random custom inserted query for a logged in user specific
    * @param records number of random records to return
    * @param userId logged in user id
    * @return Option of CustomQuotesQuery
    */
  def listRandomQuote(records: Int, userId: Int): Seq[CustomQuotesQuery] = {
    runDbAction(getRandomRecords(records, userId))
  }

  /**
    * Create a customQuotes in the table.
    * @param customQuoteForm Custom quote info
    * @param user logged in user details
    */
  def createQuote(customQuoteForm: CustomQuoteForm, user: UserDetail): CustomQuotesQuery = {
    val currentDate = new java.sql.Date(System.currentTimeMillis())
    val insertQuery = tables returning
      tables.map(_.id) into (
        (
            fields,
            id
        ) => fields.copy(id = id)
    )
    // If the ownQuote flag is false, use provided author name, else use user full name
    val author: String =
      if (customQuoteForm.ownQuote) user.name else customQuoteForm.author.getOrElse(user.name)

    val action = insertQuery += CustomQuotesQuery(
      0,
      user.id,
      customQuoteForm.quote,
      Some(author),
      customQuoteForm.genre,
      currentDate,
      customQuoteForm.ownQuote
    )
    runDbAction(action)
  }

  /**
    * @param id quote record id
    * @param userId logged in user id
    * @param customQuoteForm updated custom quote object
    * @return number of updated records, just 1 here
    */
  def updateQuote(id: Int, userId: Int, customQuoteForm: CustomQuoteForm): Try[Int] = {
    runDbActionCatchError(
      tables
        .filter(record => record.id === id && record.userId === userId)
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
    * Delete the record from the table for logged in user
    * @param id of the selected row from the CustomQuotesQuery table
    * @param userId logged user id
    */
  def deleteQuote(id: Int, userId: Int): Int = {
    runDbAction(deleteCustomQuote(id, userId))
  }

}
