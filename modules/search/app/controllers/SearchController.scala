package controllers.search

import akkaService.AkkaService
import com.krishna.util.Logging
import dao.SearchInEsDAO
import depInject.{ SecuredController, SecuredControllerComponents }
import responseHandler.EsResponseHandler._
import javax.inject.{ Inject, Singleton }
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class SearchController @Inject()(
    scc: SecuredControllerComponents,
    searchInEsDao: SearchInEsDAO,
    akkaService: AkkaService
)(
    implicit executionContext: ExecutionContext
) extends SecuredController(scc)
    with Logging {

  implicit val indexName: String = searchInEsDao.indexName

  /**
    * Write records in index name under "quotes", need to pass number of records that will be generated randomly from postgres table
    * Can only be done by Admin role
    * Duplicate records will be overridden
    * @param records will be fetched from database and store under ES
    * @return success body or exception message
    */
  def writeInEs(records: Int): Action[AnyContent] = AdminAction.async { implicit request =>
    log.info("Executing writeInEs Controller")

    searchInEsDao
      .getAndStoreQuotes(records)
      .map(responseEsResult)
      .errorRecover
  }

  def writeMigrateQuotesToEs: Action[AnyContent] = AdminAction.async { implicit request =>
    log.info("Executing writeMigrateQuotesToEs Controller")

    akkaService.bulkInsertQuotesToES
      .map(_ => Ok("Success"))
      .errorRecover
  }

  /**
    * Delete an entire index from ES
    * Can only be done by Admin role
    * @param indexName that will be deleted from ES
    * @return success body or exception message
    */
  def deleteIndex(implicit indexName: String): Action[AnyContent] = AdminAction.async {
    implicit request =>
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
}
