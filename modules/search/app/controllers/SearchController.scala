package controllers.search

import akkaService.{ AkkaService, ExtendedAkkaService }
import com.krishna.util.UtilImplicits.ErrorRecover
import com.krishna.util.Logging
import dao.SearchInEsDAO
import depInject.{ SecuredController, SecuredControllerComponents }
import responseHandler.EsResponseHandler._

import javax.inject.{ Inject, Singleton }
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class SearchController @Inject() (
  scc: SecuredControllerComponents,
  searchInEsDao: SearchInEsDAO,
  akkaService: AkkaService,
  extendedAkkaService: ExtendedAkkaService
)(implicit executionContext: ExecutionContext)
    extends SecuredController(scc)
    with Logging {

  implicit val indexName: String = searchInEsDao.indexName

  /**
    * Copy random quotes from Postgres table and store them in ES index name under "quotes"
    * Can only be done by Admin role
    * Duplicate records will be overridden
    * @param records will be fetched from Postgres quotes database and store under ES index
    * @return success body or exception message
    */
  def writeInEs(records: Int): Action[AnyContent] = AdminAction.async { implicit request =>
    log.info(s"Executing writeInEs Controller for the records size of $records")

    searchInEsDao
      .getAndStoreQuotes(records)
      .map(responseEsResult)
      .errorRecover
  }

  /**
    * Copy the records from Postgres table to ElasticSearch using the Akka Streams
    * First it will copy all the records from Postgres quotes table (can be updated to select random records
    * for development purpose)
    * Then it will run the wiki api call to get the author details for each of the record
    * Then it will store the records in elastic search index with the log for each call batch
    * @return success body or exception message
    */
  def writeMigrateQuotesToEs: Action[AnyContent] = AdminAction.async { implicit request =>
    log.info("Executing writeMigrateQuotesToEs Controller")

    akkaService
      .createCompletionAndInsert
      .map(_ => Ok("Success"))
      .errorRecover
  }

  /**
    * Akka Stream using Alpakka framework
    * First reads and convert the CSV contents to Class objects and then runs the Wiki Media Service to get the
    * author details and finally upload the records in Postgres Database using TS-Vector data type of Postgres
    * for the full text service
    * @return String to notify the response from Akka Stream
    */
  def migrateCSVRecordsToPostgres: Action[AnyContent] = AdminAction.async { implicit request =>
    log.info("Executing migrateCSVRecordsToPostgres Controller")

    extendedAkkaService
      .runCSVMigrationStream
      .map(response => Ok(s"Response from Akka Stream: $response"))
      .errorRecover
  }

  /**
    * Delete an entire index "quotes" from ES
    * Can only be done by Admin role
    * @return success body or exception message
    */
  def deleteIndex(): Action[AnyContent] = AdminAction.async { implicit request =>
    log.info(s"Executing deleteIndex controller for index: $indexName")
    log.warn("Hope you know what you are doing!")

    searchInEsDao
      .deleteQuotesIndex(indexName)
      .map(responseEsResult)
      .errorRecover
  }

  /**
    * List of the quotes that match the search text
    * @return Returns seq of matched quote
    */
  def searchQuote(text: String): Action[AnyContent] = UserAction.async { implicit request =>
    log.info(s"Executing searchQuote controller")

    val limit: Int =
      request.getQueryString("limit").map(_.toInt).getOrElse(10)
    val offset: Int =
      request.getQueryString("offset").map(_.toInt).getOrElse(0)

    log.info(s"Searching for text: $text with offset: $offset and limit of $limit")

    searchInEsDao
      .searchQuote(text, offset, limit)
      .map(responseEsResult)
      .errorRecover
  }

  /**
    * Use of the ElasticSearch Auto Completion API to get auto completion for the authors name
    * Have used the fuzziness of the size 2, get the suggestions if there are any typos
    * @param author users searched parameter
    * @return list of the author names as an auto completion
    */
  def getAuthorsAutoSuggestion(author: String): Action[AnyContent] = UserAction.async { _ =>
    log.info(
      s"Searching for author: $author to retrieve auto completion and suggestion for any spell mistake"
    )

    searchInEsDao
      .completeAuthorNames(author)
      .map(responseEsResult)
      .errorRecover
  }

}
