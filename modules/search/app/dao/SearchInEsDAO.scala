package dao

import com.krishna.model.QuotesQuery
import com.krishna.util.Logging
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import com.sksamuel.elastic4s.requests.searches.{ SearchRequest, SearchResponse }
import daos.QuoteQueryDAO
import play.api.Configuration
import util.InitEs

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class SearchInEsDAO @Inject()(quotesDAO: QuoteQueryDAO, config: Configuration)(
    implicit ec: ExecutionContext
) extends InitEs
    with Logging {

  override val elasticHost: String = sys.env.getOrElse("ES_HOST", config.get[String]("ES.ES_HOST"))
  override val elasticPort: String = sys.env.getOrElse("ES_PORT", config.get[String]("ES.ES_PORT"))
  override val indexName: String   = config.get[String]("ES.ES_INDEX_NAME")

  /**
    * Gets the records from Postgres table and store in ES
    * @param records Number of records to be stored in ES (max?)
    * @return Response once records are stored in ES
    */
  def getAndStoreQuotes(records: Int): Seq[Future[Response[IndexResponse]]] = {
    log.info(s"Getting $records random quotes from database")
    val quotes: Seq[QuotesQuery] = quotesDAO.listRandomQuote(records)

    // if the index exist, have to wait until the index is deleted without any error
    if (doesIndexExists) deleteQuotesIndex(indexName).await

    log.info(s"Creating a new index in the ES: $indexName")
    quotes.map { quote =>
      client.execute {
        // providing index with csvId to avoid duplicates records with same csvId
        val csvId = quote.csvId
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
    log.warn(s"Searching text : $text in the index: $indexName")
    client
      .execute(
        searchRequest(text)
          .from(offset)
          .size(limit)
      )
  }

  // Move to quotesDao class
  def searchAuthorsSql(text: String): Seq[String] = {
    quotesDAO.searchAuthors(text)
  }

  // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-prefix-query.html
  val searchRequest: String => SearchRequest = (text: String) =>
    search(indexName).query(matchPhrasePrefixQuery("quote", s"$text"))

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
}
