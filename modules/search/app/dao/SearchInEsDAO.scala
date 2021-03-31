package dao

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.krishna.model.QuotesQuery
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.bulk.BulkResponse
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import daos.QuoteQueryDAO
import play.api.Configuration
import config.ElasticsearchConfig
import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future, Promise }

class SearchInEsDAO @Inject()(
    system: ActorSystem,
    quotesDAO: QuoteQueryDAO,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends SearchMethods {

  override val esConfig: ElasticsearchConfig =
    config.get[ElasticsearchConfig]("elasticsearch")

  /**
    * Gets the records from Postgres table and store in ES
    * @param records Number of records to be stored in ES (max?)
    * @return Response once records are stored in ES
    */
  def getAndStoreQuotes(records: Int): Future[Response[BulkResponse]] = {
    log.info(s"Getting $records random quotes from database")
    val quotes: Seq[QuotesQuery] = quotesDAO.listRandomQuote(records)

    // if the index exist, have to wait until the index is deleted without any error
    if (doesIndexExists) deleteQuotesIndex(indexName).await

    log.info(s"Creating a new index in the ES: $indexName")

    // providing index with csvId to avoid duplicates records with same csvId
    // if createOnly set to true then trying to update a document will fail
    // have set as false (default) so that duplicate records can override the existing records

    client.execute {
      bulk {
        quotes.map { quote =>
          indexInto(indexName).id(quote.csvId).doc(quote)
        }
      }.refresh(RefreshPolicy.Immediate)

    }
  }

  def bulkInsertQuotesToES = {
    val promise = Promise[Unit]()

    val esSink: Sink[QuotesQuery, NotUsed] = Sink.fromSubscriber {
      client.subscriber[QuotesQuery](batchSize = 500, completionFn = { () =>
        promise.success(()); ()
      }, errorFn = { (t: Throwable) =>
        promise.failure(t); ()
      })(builder(indexName), system)
    }
  }

  /**
    * Delete the index in elastic search with given index name
    * @param indexName -> Index to delete
    * @return -> Future response
    */
  def deleteQuotesIndex(indexName: String): Future[Response[DeleteIndexResponse]] = {
    log.warn(s"Deleting the index: $indexName")
    client.execute(
      deleteIndex(indexName)
    )
  }

  /**
    * Text search using ES search API
    * @param text -> text to search, does any
    * @param offset -> For pagination, default to 0
    * @param limit -> Have to be greater than 1, default to 10
    * @return Search response that have matched quotes
    */
  def searchQuote(
      text: String,
      offset: Int = 0,
      limit: Int = 10
  ): Future[Response[SearchResponse]] = {
    log.info(s"Searching text : $text in the index: $indexName")
    client
      .execute(
        searchRequest(text)
          .from(offset)
          .size(limit)
      )
  }
}
