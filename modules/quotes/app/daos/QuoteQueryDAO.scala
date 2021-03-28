package daos

import akka.NotUsed
import akka.stream.scaladsl.{ Flow, Sink, Source }
import com.krishna.model.Genre.Genre
import com.krishna.model.QuotesQuery
import com.krishna.services.RepositoryQuoteMethods
import com.krishna.util.DbRunner
import com.krishna.util.Implicits._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{ JdbcBackend, JdbcProfile, ResultSetConcurrency, ResultSetType }
import tables.QuoteQueriesTable
import javax.inject.{ Inject, Singleton }

import scala.concurrent.Future

/**
  * A repository for Quotes stored in quotes table.
  */
@Singleton
class QuoteQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)
    extends DbRunner
    with RepositoryQuoteMethods[QuotesQuery, QuoteQueriesTable] {

  override val dbConfig: JdbcBackend#DatabaseDef = dbConfigProvider.get[JdbcProfile].db

  override def tables: TableQuery[QuoteQueriesTable] = QuoteQueriesTable.quoteQueries

  /**
    * @return List of all stored quotes from database
    */
  def listAllQuotes: Seq[QuotesQuery] = {
    log.info("Getting all the records from the table")
    runDbAction(getAllQuotes)
  }

  /**
    * Create an akka-streams Source from a reactive-streams publisher,
    * entering akka-streams land where we get access to a richer API for stream element processing
    * Execution of the DBIOAction does not start until a Subscriber is attached to the stream.
    * Note: https://scala-slick.org/doc/3.2.0/dbio.html#streaming
    */
  def allQuotesSource: Source[QuotesQuery, NotUsed] = Source.fromPublisher {
    dbConfig.stream {
      getAllQuotes
        .withStatementParameters(
          rsType = ResultSetType.ForwardOnly,
          rsConcurrency = ResultSetConcurrency.ReadOnly,
          fetchSize = 500
        )
        .transactionally
    }
  }

  /**
    * Construct a Flow[QuotesQuery] that emits Quote elements from a function
    * that returns a Future[QuotesQuery].
    * Parallelism of the Future-producing call is controlled under the hood by the actor
    * behind the Flow.
    */
  def addAuthorDetails(
      getAuthorDetails: QuotesQuery => Future[QuotesQuery]
  ): Flow[QuotesQuery, QuotesQuery, NotUsed] = {
    Flow[QuotesQuery].mapAsync(parallelism = 5) { quote =>
      getAuthorDetails(quote)
    }
  }

  /**
    * Sink that indiscriminately tallies up and prints the count of elements it has seen.
    */
  def logElementsPerBlock[T]: Sink[T, Future[Int]] = {
    Sink.fold[Int, T](0) { (sum, _) =>
      val newSum = sum + 1
      if (newSum % 500 == 0) {
        log.info(s"\rCount: $newSum")
      }
      newSum
    }
  }

  /**
    * @param records number of records to return
    * @return Random quote from the database table
    */
  def listRandomQuote(records: Int): Seq[QuotesQuery] = {
    log.info(s"Getting random quotes from the table with total size: $records")
    runDbAction(getRandomRecords(records))
  }

  /**
    * @param genre to filter records with that genre
    * @return Random quote that matches input genre
    */
  def listGenreQuote(genre: Genre): Option[QuotesQuery] = {
    log.info(s"Getting random quote from the table with genre: ${genre.value}")
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
        .groupBy(_.author)
        .map { case (author, _) => author }
        .filter(_.toLowerCase like s"%$inputParam%")
        .result
    ).flatten
      .sortBy(!_.startsWith(inputParam.take(1).capitalize)) //Sorted by the first letter of parameter
      .take(10)
  }
}
