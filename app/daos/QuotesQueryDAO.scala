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
    def index: Rep[Int] = column[Int]("index")
    def author: Rep[String] = column[String]("author")
    def quote: Rep[String] = column[String]("quote")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the QuotesQuery object.
     *
     * In this case, we are simply passing the index, author and quote parameters to the QuotesQuery case classes
     * apply and unapply methods.
     */
    def * : ProvenShape[QuotesQuery] = (index, author, quote) <> ((QuotesQuery.apply _).tupled, QuotesQuery.unapply)
  }

  /**
   * The starting point for all queries on the people table.
   */
  private val QuoteQueries = TableQuery[QuoteQueriesTable]

  def list(): Future[Seq[QuotesQuery]] = db.run(QuoteQueries.result)
}
