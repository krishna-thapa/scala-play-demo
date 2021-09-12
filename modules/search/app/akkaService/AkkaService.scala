package akkaService

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer.matFromSystem
import akka.stream.scaladsl.Sink
import com.krishna.akkaStream.StreamsInit
import com.krishna.model.QuotesQuery
import com.sksamuel.elastic4s.ElasticApi.RichFuture
import com.sksamuel.elastic4s.streams.ReactiveElastic.ReactiveElastic
import config.ElasticsearchConfig
import dao.CommonEsMethods
import daos.QuoteQueryDAO
import httpService.WikiMediaApi
import models.QuoteWithAuthor
import play.api.Configuration

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future, Promise }

class AkkaService @Inject()(
    quotesDAO: QuoteQueryDAO,
    wikiMediaApi: WikiMediaApi,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends CommonEsMethods
    with StreamsInit {

  override def batchSize: Int = config.get[Int]("wiki.batchSize")

  override val esConfig: ElasticsearchConfig =
    config.get[ElasticsearchConfig]("elasticsearch")

  override val dbConfig = quotesDAO.dbConfig

  implicit lazy val system: ActorSystem = ActorSystem()

  def createCompletionAndInsert: Future[Unit] = {

    // if the index exist, have to wait until the index is deleted without any error
    if (doesIndexExists) deleteQuotesIndex(indexName).await
    log.info(s"Creating a new index in the ES: $indexName")

    // First create a Index with ES completion field and then insert quotes on the index
    createIndexWithCompletionField.flatMap { _ =>
      bulkInsertQuotesToES
    }
  }

  // Akk Stream to get the records from Postgres and Call WIKI Api and then store the results in ES
  def bulkInsertQuotesToES: Future[Unit] = {
    val promise = Promise[Unit]()

    val esSink: Sink[QuoteWithAuthor, NotUsed] = Sink.fromSubscriber {
      client.subscriber[QuoteWithAuthor](
        batchSize,
        completionFn = { () =>
          log.info(
            s"Completed: Records are stored in the elastic search db under index: $indexName"
          )
          promise.success(()); ()
        },
        errorFn = { (t: Throwable) =>
          log.error(
            s"Error while running akk stream for uploading data from Postgres to ES, " +
              s"error: ${t.getMessage}"
          )
          promise.failure(t); ()
        }
      )(QuoteWithAuthor.builder(indexName), system)
    }

    // Create a full akka stream graph
    recordsSource[QuotesQuery](quotesDAO.getRandomRecords(500)) // TODO: update with getAllQuotes
      .via(addFlowPerRecord[QuotesQuery, QuoteWithAuthor](wikiMediaApi.getWikiResponse))
      .alsoTo(logElementsPerBlock)
      .runWith(esSink)

    promise.future
  }
}
