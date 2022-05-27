package dao

import com.krishna.util.Logging
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.requests.indexes.CreateIndexResponse
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import com.sksamuel.elastic4s.requests.mappings.{
  CompletionField,
  MappingDefinition,
  ObjectField,
  TextField
}
import com.sksamuel.elastic4s.requests.searches.queries.matches.MatchPhrasePrefix
import com.sksamuel.elastic4s.requests.searches.sort.ScoreSort
import com.sksamuel.elastic4s.requests.searches.sort.SortOrder.Desc
import com.sksamuel.elastic4s.requests.searches.{ SearchRequest, SearchResponse }
import config.{ InitEs, SearchSuggestion }

import scala.concurrent.Future

trait CommonEsMethods extends InitEs with Logging {

  def searchSuggestion: SearchSuggestion

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
    * Check if the index is present in the ElasticSearch
    * @return a boolean
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
    Create an index and a custom field with the type of completion field and copy the author data on it
    We can copy data on that completion field so that can be used for auto completion
    https://www.elastic.co/guide/en/elasticsearch/reference/5.5/copy-to.html
   */
  def createIndexWithCompletionField: Future[Response[CreateIndexResponse]] = {
    log.info(s"Creating the index: $indexName with author as a completion field")
    client.execute {
      createIndex(indexName).mapping(
        MappingDefinition(
          Seq(
            CompletionField(searchSuggestion.suggestionColumnName),
            ObjectField("quoteDetails").fields(
              Seq(TextField("author").copyTo(searchSuggestion.suggestionColumnName))
            )
          )
        )
      )
    }
  }

  /*
    Use the ElasticSearch Match query API with the match prefix search
    @param: isSourceInclude is this is true then instead or returning matched record from index, it will
    only returns the matched data from the sources included column
   */
  def matchPrefixSearch(
    inputText: String,
    columnName: String,
    offset: Int = 0,
    limit: Int = 10,
    isSourceInclude: Boolean = false
  ): Future[Response[SearchResponse]] = {
    log.info(
      s"Searching match prefix in index: $indexName for searched text: $inputText in column field: $columnName"
    )

    val query: MatchPhrasePrefix = matchPhrasePrefixQuery(s"quoteDetails.$columnName", inputText)
    val searchRequest: SearchRequest = search(indexName).query(query)

    val updatedSearchRequest: SearchRequest = if (isSourceInclude) {
      // It only returns the search response from the column where we are searching instead of returning
      // the whole record of quote details
      searchRequest.sourceInclude(Seq(s"quoteDetails.$columnName"))
    } else searchRequest
    client.execute(
      updatedSearchRequest
        .from(offset)
        .size(limit)
        .sortBy(ScoreSort(Desc))
    )
  }

}
