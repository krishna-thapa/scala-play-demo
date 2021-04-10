package akkaService

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.krishna.akkaStream.StreamsInit
import com.krishna.model.QuotesQuery
import com.sksamuel.elastic4s.streams.ReactiveElastic.ReactiveElastic
import config.{ ElasticsearchConfig, InitEs }
import daos.QuoteQueryDAO
import httpService.WikiMediaApi
import javax.inject.Inject
import models.AuthorDetails
import play.api.Configuration

import scala.concurrent.Promise

class AkkaService @Inject()(
    system: ActorSystem,
    quotesDAO: QuoteQueryDAO,
    wikiMediaApi: WikiMediaApi,
    config: Configuration
) extends InitEs
    with StreamsInit {

  override def batchSize: Int = config.get[Int]("")

  override val esConfig: ElasticsearchConfig =
    config.get[ElasticsearchConfig]("elasticsearch")

  override val dbConfig = quotesDAO.dbConfig

  def bulkInsertQuotesToES = {
    val promise = Promise[Unit]()

    val esSink: Sink[QuotesQuery, NotUsed] = Sink.fromSubscriber {
      client.subscriber[QuotesQuery](batchSize, completionFn = { () =>
        promise.success(()); ()
      }, errorFn = { (t: Throwable) =>
        promise.failure(t); ()
      })(builder(indexName), system)
    }

    // Create a full akka stream graph
    recordsSource[QuotesQuery](quotesDAO.getAllQuotes)
      .via(addFlowPerRecord[QuotesQuery, AuthorDetails](wikiMediaApi.getWikiResponse))
  }
}
