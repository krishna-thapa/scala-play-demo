package daos

import java.sql.Date

import javax.inject.{Inject, Singleton}
import models.Genre.Genre
import models.{CustomQuoteForm, CustomQuotesQuery, Genre}
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.BaseTypedType
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
class CustomQuotesQueryDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext
) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  implicit val genreEnumMapper: BaseTypedType[Genre.Value] =
    MappedColumnType.base[Genre, String](_.toString, Genre.withName)

  private class CustomQuotesQueriesTable(tag: Tag)
      extends Table[CustomQuotesQuery](tag, "custom_quotations") {

    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def quote: Rep[String] = column[String]("quote")
    def author: Rep[String] = column[String]("author")
    def genre: Rep[Genre] = column[Genre]("genre")
    def storeddate: Rep[Date] = column[Date]("storeddate")
    def ownquote: Rep[Boolean] = column[Boolean]("ownquote")
    def * : ProvenShape[CustomQuotesQuery] =
      (id, quote, author, genre, storeddate, ownquote) <>
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
  def listCustomQuotes(): Future[Seq[CustomQuotesQuery]] =
    db.run(customQuoteQueries.sortBy(_.id).result)

  /**
    * List the JSON format of the selected record from the table
    * @param id quote id
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
  def createQuote(customQuote: CustomQuoteForm): Future[CustomQuotesQuery] = {
    val currentDate = new java.sql.Date(System.currentTimeMillis())
    val insertQuery = customQuoteQueries returning customQuoteQueries.map(_.id) into (
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
    db.run(action)
  }

  // return number of records get updated
  def updateQuote(id: Int, customQuote: CustomQuoteForm): Future[Int] = {
    db.run(
      customQuoteQueries
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
    db.run(customQuoteQueries.filter(_.id === id).delete)
  }
}
