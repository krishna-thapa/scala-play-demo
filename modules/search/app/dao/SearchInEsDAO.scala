package dao

import com.krishna.model.QuotesQuery
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.bulk.BulkResponse
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.searches.sort.ScoreSort
import com.sksamuel.elastic4s.requests.searches.sort.SortOrder.Desc
import com.sksamuel.elastic4s.requests.searches.suggestion.CompletionSuggestion
import com.sksamuel.elastic4s.requests.searches.suggestion.Fuzziness.Two
import com.sksamuel.elastic4s.requests.searches.{ SearchRequest, SearchResponse }
import config.{ ElasticsearchConfig, SuggestionName }
import daos.QuoteQueryDAO
import models.QuoteWithAuthor
import play.api.Configuration

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class SearchInEsDAO @Inject()(
    quotesDAO: QuoteQueryDAO,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends CommonEsMethods {

  override val esConfig: ElasticsearchConfig =
    config.get[ElasticsearchConfig]("elasticsearch")

  /**
    * Gets the records from Postgres table and store in ES
    * @param records Number of records to be stored in ES (max?)
    * @return Response once records are stored in ES
    */
  def getAndStoreQuotes(records: Int): Future[Response[BulkResponse]] = {
    log.info(s"Getting $records random quotes from postgres database")
    val quotes: Seq[QuotesQuery] = quotesDAO.listRandomQuote(records)

    // if the index exist, have to wait until the index is deleted without any error
    if (doesIndexExists) deleteQuotesIndex(indexName).await

    log.info(s"Creating a new index in the ES: $indexName")

    // providing index with csvId to avoid duplicates records with same csvId
    // if createOnly set to true then trying to update a document will fail
    // have set as false (default) so that duplicate records can override the existing records

    createIndexWithCompletionField.flatMap { _ =>
      client.execute {
        bulk {
          quotes.map { quote =>
            indexInto(indexName).id(quote.csvId).doc(QuoteWithAuthor(quote))
          }
        }.refresh(RefreshPolicy.Immediate)
      }
    }
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
          .sortBy(ScoreSort(Desc))
      )
  }

  /*
    The match query is the standard query for performing a full-text search, including options for fuzzy matching.
    https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-match-query.html
   */
  private def searchRequest(text: String): SearchRequest = {
    val query = matchQuery("quoteDetails.quote", s"$text")
      .fuzziness("AUTO")
      .operator("AND")
    search(indexName).query(query)
  }

  // https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters.html#completion-suggester
  def completeAuthorNames(author: String): Future[Response[SearchResponse]] = {
    log.info(s"Auto completion author name : $author in the index: $indexName")

    val authorCompletion: CompletionSuggestion =
      CompletionSuggestion(SuggestionName.completionAuthor.toString, "suggest_author")
        .text(author)
        .size(5)
        .fuzziness(Two)
        .skipDuplicates(true)

    client
      .execute(
        search(indexName)
          .suggestion(authorCompletion)
      )
  }
}
