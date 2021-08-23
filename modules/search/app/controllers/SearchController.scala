package controllers.search

import akkaService.AkkaService
import com.krishna.util.FutureErrorHandler.{ ErrorRecover, ToFuture }
import com.krishna.util.Logging
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.requests.searches.{
  CompletionSuggestionOption,
  SearchResponse,
  TermSuggestionOption
}
import config.SuggestionName
import dao.SearchInEsDAO
import depInject.{ SecuredController, SecuredControllerComponents }
import models.{ AuthorCompletion, CompletionResponseType }
import play.api.libs.json.Json
import responseHandler.EsResponseHandler._

import javax.inject.{ Inject, Singleton }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

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
    * Copy random quotes from Postgres table and store them in ES index name under "quotes" index
    * Can only be done by Admin role
    * Duplicate records will be overridden
    * @param records will be fetched from Postgres quotes database and store under ES index
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

  def getAuthorsAutoSuggestion(author: String): Action[AnyContent] = UserAction.async { _ =>
    log.info(
      s"Searching for author: $author to retrieve auto completion and suggestion for any spell mistake"
    )

    searchInEsDao
      .completeAuthorNames(author)
      .flatMap(response => getCompletionAuthor(response, author))
      .errorRecover
  }

  // create class for do you mean response type
  private def getCompletionAuthor(
      response: Response[SearchResponse],
      author: String
  ): Future[Result] = {
    log.info(s"Getting auto completion for the searched text for author: $author")

    val searchResponse: Seq[CompletionSuggestionOption] =
      response.result
        .suggestions(SuggestionName.completionAuthor.toString)
        .flatMap(_.toCompletion.options)
    if (searchResponse.isEmpty) getSuggestedAuthor(author)
    else {
      val results: Seq[String] = searchResponse.map(_.text)
      Ok(Json.toJson(AuthorCompletion(CompletionResponseType.AutoCompletion, results))).toFuture
    }
  }

  private def getSuggestedAuthor(author: String): Future[Result] = {
    log.warn(
      s"There are no completion for the searched author text, getting any suggestion for author: $author"
    )
    searchInEsDao
      .suggestAuthorNames(author)
      .map { suggestion =>
        val searchResponse: Seq[TermSuggestionOption] =
          suggestion.result
            .suggestions(SuggestionName.suggestAuthor.toString)
            .flatMap(_.toTerm.options)
        log.warn(
          s"Debugging: ${suggestion.result}"
        )
        if (searchResponse.isEmpty) NotFound("Not found")
        else {
          val result = searchResponse.map(_.text).head
          // Pass to the getCompletionAuthor to get the complete author name, use flag
          Ok(Json.toJson(AuthorCompletion(CompletionResponseType.PhraseSuggestion, Seq(result))))
        }
      }
      .errorRecover
  }
}
