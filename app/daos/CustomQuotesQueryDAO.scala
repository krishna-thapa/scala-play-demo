package daos

import java.sql.Date

import javax.inject.{Inject, Singleton}
import models.CustomQuotesQuery
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

/**
 * A repository for the custom quotes
 *
 * This class has a `Singleton` annotation because we need to make
 * sure we only use one CustomQuotesQueryDAO per application. Without this
 * annotation we would get a new instance every time a [[CustomQuotesQueryDAO]] is
 * injected.
 */

@Singleton
class CustomQuotesQueryDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class CustomQuotesQueriesTable(tag: Tag) extends Table[CustomQuotesQuery](tag, "custom_quotations") {

    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def quote: Rep[String] = column[String]("quote")
    def author: Rep[String] = column[String]("author")
    def genre: Rep[String] = column[String]("genre")
    def storedDate: Rep[Date] = column[Date]("storeddate")
    def ownQuote: Rep[Boolean] = column[Boolean]("ownquote")
    def * : ProvenShape[CustomQuotesQuery] = (id, quote, author, genre, storedDate, ownQuote) <>
      ((CustomQuotesQuery.apply _).tupled, CustomQuotesQuery.unapply)
  }

  /**
   * The starting point for all queries on the CustomQuotesQuries table.
   */
  private val customQuoteQueries = TableQuery[CustomQuotesQueriesTable]

  /**
   * List all the records from the table
   * @return sequence of the CustomQuotesQuery records
   */
  def listCustomQuotes(): Future[Seq[CustomQuotesQuery]] = db.run(customQuoteQueries.sortBy(_.id).result)

  /**
   * List the JSON format of the selected record from the table
   * @param id
   * @return Option of the CustomQuotesQuery record
   */
  def listSelectedQuote(id: Int): Future[Option[CustomQuotesQuery]] = {
    db.run(customQuoteQueries.filter(_.id === id).result.headOption)
  }

  /**
   * Defined custom function for slick 3
   * aware that "random" function is database specific
   * @return Option of CustomQuotesQuery
   */
  def listRandomQuote(): Future[Option[CustomQuotesQuery]] = {
    val randomFunction = SimpleFunction.nullary[Double]("random")
    db.run(customQuoteQueries.sortBy(x => randomFunction).result.headOption)
  }

  /**
   * Create a customQuotes in the table.
   *
   * This is an asynchronous operation, it will return a future of the created customQuotes,
   * which can be used to obtain the id for that person.
   */
  def createQuote(customQuote: CustomQuotesQuery): Future[CustomQuotesQuery] = {
    db.run(customQuoteQueries returning customQuoteQueries.map(_.id)
     += customQuote).map { id => customQuote.copy(id = id) }
  }

  def updateQuote(customQuote: CustomQuotesQuery): Future[Int] = {
    db.run(customQuoteQueries.filter(_.id === customQuote.id).map( quote => (quote.quote, quote.author, quote.genre, quote.ownQuote))
    .update(customQuote.quote, customQuote.author, customQuote.genre, customQuote.ownQuote))
  }
  /**
   * Delete the record from the table
   * @param id of the selected row from the CustomQuotesQuery table
   */
  def deleteQuote(id: Int): Unit = {
    db.run(customQuoteQueries.filter(_.id === id).delete)
  }
}
