package daos

import javax.inject.{ Inject, Singleton }
import models.QuotesQuery
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{ ExecutionContext, Future }

/**
 * A repository for Quotes.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 * We want the JdbcProfile for this provider
 */
@Singleton
class QuotesQueryDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(
  implicit executionContext: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  /**
  * Here we define the table. It will have a quotations
  */
  private class QuoteQueriesTable(tag: Tag) extends Table[QuotesQuery](tag, "quotations") {
    def id: Rep[Int] = column[Int]("id")
    def quote: Rep[String] = column[String]("quote")
    def author: Rep[String] = column[String]("author")
    def genre: Rep[String] = column[String]("genre")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the QuotesQuery object.
     *
     * In this case, we are simply passing the id, quote, author and genre parameters to the QuotesQuery case classes
     * apply and unapply methods.
     */
    def * : ProvenShape[QuotesQuery] = (id, quote, author, genre) <> ((QuotesQuery.apply _).tupled, QuotesQuery.unapply)
  }

  /**
   * The starting point for all queries on the people table.
   */
  private val QuoteQueries = TableQuery[QuoteQueriesTable]

  def list(): Future[Seq[QuotesQuery]] = db.run(QuoteQueries.result)
}
