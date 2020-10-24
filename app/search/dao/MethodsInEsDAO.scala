package search.dao

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import daos.QuoteQueryDAO
import javax.inject.Inject
import models.QuotesQuery
import _root_.search.util.InitEs
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import com.sksamuel.elastic4s.requests.searches.{ SearchRequest, SearchResponse }
import utils.Logging

import scala.concurrent.{ ExecutionContext, Future }

class MethodsInEsDAO @Inject()(quotesDAO: QuoteQueryDAO)(implicit ec: ExecutionContext)
    extends InitEs
    with Logging {

  def getAndStoreQuotes(records: Int): Seq[Future[Response[IndexResponse]]] = {
    log.info(s"Getting $records random quotes from database")
    val quotes: Seq[QuotesQuery] = quotesDAO.listRandomQuote(records)
    quotes.map { quote =>
      client.execute {
        // providing index with csvId to avoid duplicates records with same csvId
        val csvId = quote.csvid
        // if createOnly set to true then trying to update a document will fail
        // have set as false (default) so that duplicate records can override the existing records
        indexInto(indexName).id(csvId).doc(quote).refresh(RefreshPolicy.Immediate)
      }
    }
  }

  def deleteQuotesIndex(indexName: String): Future[Response[DeleteIndexResponse]] = {
    log.warn(s"Deleting the index: $indexName")
    client.execute(
      deleteIndex(indexName)
    )
  }

  def searchQuote(text: String, offset: Int, limit: Int): Future[Response[SearchResponse]] = {
    log.warn(s"Searching text : $text in the index: $indexName")
    client
      .execute(
        searchRequest(text)
          .from(offset)
          .size(limit)
      )
  }

  // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-prefix-query.html
  val searchRequest: String => SearchRequest = (text: String) =>
    search(indexName).query(matchPhrasePrefixQuery("quote", s"$text"))

}
