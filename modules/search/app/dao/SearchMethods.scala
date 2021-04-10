package dao

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.requests.bulk.BulkResponse
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import com.sksamuel.elastic4s.requests.searches.{ SearchRequest, SearchResponse }

import scala.concurrent.Future

trait SearchMethods extends CommonEsMethods {

  def getAndStoreQuotes(records: Int): Future[Response[BulkResponse]]

  def deleteQuotesIndex(indexName: String): Future[Response[DeleteIndexResponse]]

  def searchQuote(
      text: String,
      offset: Int,
      limit: Int
  ): Future[Response[SearchResponse]]

  /*
  Use search API query to match phrase prefix
  https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-prefix-query.html
   */
  def searchRequest(text: String): SearchRequest = {
    search(indexName).query(matchPhrasePrefixQuery("quote", s"$text"))
  }
}
