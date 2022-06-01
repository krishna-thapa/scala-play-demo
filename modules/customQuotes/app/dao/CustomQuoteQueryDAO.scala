package dao

import com.krishna.response.ErrorMsg
import com.krishna.services.RepositoryUserMethods
import com.krishna.util.DbRunner
import com.krishna.util.Implicits.genreEnumMapper
import config.DecodeHeader
import forms.RequestForm.CustomQuoteForm
import model.UserDetail
import models.CustomQuotesQuery
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.{ AnyContent, Request }
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{ JdbcBackend, JdbcProfile }
import table.CustomQuotesQueriesTable

import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future

/**
  * A repository for the custom quotes
  *
  * This class has a `Singleton` annotation because we need to make
  * sure we only use one CustomQuotesQueryDAO per application. Without this
  * annotation we would get a new instance every time a [[CustomQuoteQueryDAO]] is
  * injected.
  */

@Singleton
class CustomQuoteQueryDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit
  config: Configuration
) extends RepositoryUserMethods[CustomQuotesQuery, CustomQuotesQueriesTable]
    with DbRunner {

  override val dbConfig: JdbcBackend#DatabaseDef = dbConfigProvider.get[JdbcProfile].db

  override def tables: TableQuery[CustomQuotesQueriesTable] =
    CustomQuotesQueriesTable.customQuoteQueries

  /**
    * List all the CustomQuotesQuery records from the table
    * @param userId: Logged in user id
    * @return sequence of the CustomQuotesQuery records
    */
  def listAllQuotes(userId: Int): Future[Seq[CustomQuotesQuery]] =
    runDbAsyncAction(getAllQuotesForUser(userId))

  /**
    * List the selected CustomQuotesQuery record from the table
    * @param id quote id
    * @param userId logged in user id
    * @return Option of the CustomQuotesQuery record
    */
  def listSelectedQuote(id: Int, userId: Int): Future[Option[CustomQuotesQuery]] =
    runDbAsyncAction(getSelectedQuote(id, userId))

  /**
    * Return a random CustomQuotesQuery record for a logged in user
    * @param records number of random records to return
    * @param userId logged in user id
    * @return Option of the CustomQuotesQuery record
    */
  def listRandomQuote(records: Int, userId: Int): Future[Seq[CustomQuotesQuery]] =
    runDbAsyncAction(getRandomRecords(records, userId))

  /**
    * Create a customQuotes in the table.
    * @param customQuoteForm Custom quote info
    * @param user logged in user details
    */
  def createQuote(customQuoteForm: CustomQuoteForm, user: UserDetail): Future[CustomQuotesQuery] = {
    val currentDate = new java.sql.Date(System.currentTimeMillis())
    val insertQuery = tables returning
      tables.map(_.id) into ((fields, id) => fields.copy(id = id))
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
    runDbAsyncAction(action)
  }

  /**
    * @param id quote record id
    * @param userId logged in user id
    * @param customQuoteForm updated custom quote object
    * @return number of updated records, just 1 here
    */
  def updateQuote(id: Int, userId: Int, customQuoteForm: CustomQuoteForm): Future[Int] = {
    runDbAsyncAction(
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
  def deleteQuote(id: Int, userId: Int): Future[Int] = {
    runDbAsyncAction(deleteCustomQuote(id, userId))
  }

  /**
    * Decode Header that returns User details id request has right Auth token
    * @param request With header contents
    * @return Either error message or User details
    */
  def decoderHeader(request: Request[AnyContent]): Either[ErrorMsg, UserDetail] = {
    DecodeHeader(request.headers)
  }

}
