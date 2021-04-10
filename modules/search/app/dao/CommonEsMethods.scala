package dao

import com.krishna.util.Logging
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import config.InitEs

import scala.concurrent.Future

trait CommonEsMethods extends InitEs with Logging {

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
}
