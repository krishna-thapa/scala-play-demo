package dao

import com.krishna.model.QuotesQuery
import com.krishna.util.Logging
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import com.sksamuel.elastic4s.requests.searches.{ SearchRequest, SearchResponse }
import com.sksamuel.elastic4s.streams.RequestBuilder
import config.InitEs

import scala.concurrent.Future

trait SearchMethods extends InitEs with Logging {

  def deleteQuotesIndex(indexName: String): Future[Response[DeleteIndexResponse]]

  def searchQuote(
      text: String,
      offset: Int,
      limit: Int
  ): Future[Response[SearchResponse]]

  /*
  An implementation of RequestBuilder to load stream in ElasticSearch
   */
  def builder(indexName: String): RequestBuilder[QuotesQuery] =
    (q: QuotesQuery) => indexInto(indexName).id(q.csvId).doc(q).refresh(RefreshPolicy.Immediate)

  /*
  Count the total docs inside the index, used for testing
   */
  def countDocsInIndex: Long = {
    client
      .execute {
        count(indexName)
      }
      .await
      .result
      .count
  }

  /*
  Check if the index is present in the ElasticSearch
  Returns a boolean
   */
  def doesIndexExists: Boolean = {
    log.info(s"Checking if the index: $indexName exists already")
    client
      .execute {
        indexExists(indexName)
      }
      .await
      .result
      .isExists
  }

  /*
  Use search API query to match phrase prefix
  https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-prefix-query.html
   */
  def searchRequest(text: String): SearchRequest = {
    search(indexName).query(matchPhrasePrefixQuery("quote", s"$text"))
  }
}
