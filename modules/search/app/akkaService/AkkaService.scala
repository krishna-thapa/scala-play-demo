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
import javax.inject.Inject
import models.QuoteWithAuthor
import play.api.Configuration

import scala.concurrent.Promise

class AkkaService @Inject()(
    quotesDAO: QuoteQueryDAO,
    wikiMediaApi: WikiMediaApi,
    config: Configuration
) extends CommonEsMethods
    with StreamsInit {

  override def batchSize: Int = config.get[Int]("wiki.batchSize")

  override val esConfig: ElasticsearchConfig =
    config.get[ElasticsearchConfig]("elasticsearch")

  override val dbConfig = quotesDAO.dbConfig

  implicit lazy val system: ActorSystem = ActorSystem()

  def bulkInsertQuotesToES = {

    // if the index exist, have to wait until the index is deleted without any error
    if (doesIndexExists) deleteQuotesIndex(indexName).await
    log.info(s"Creating a new index in the ES: $indexName")

    val promise = Promise[Unit]()

    val esSink: Sink[QuoteWithAuthor, NotUsed] = Sink.fromSubscriber {
      client.subscriber[QuoteWithAuthor](batchSize, completionFn = { () =>
        promise.success(()); ()
      }, errorFn = { (t: Throwable) =>
        promise.failure(t); ()
      })(QuoteWithAuthor.builder(indexName), system)
    }

    // Create a full akka stream graph
    recordsSource[QuotesQuery](quotesDAO.getRandomRecords(300))
      .via(addFlowPerRecord[QuotesQuery, QuoteWithAuthor](wikiMediaApi.getWikiResponse))
      .alsoTo(logElementsPerBlock)
      .runWith(esSink)

    promise.future
  }
}
