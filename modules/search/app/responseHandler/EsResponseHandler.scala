package responseHandler

import com.krishna.response.ErrorMsg.{ EmptyDbMsg, EsPlaceHolder }
import com.krishna.response.ResponseError
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.playjson.playJsonHitReader
import com.sksamuel.elastic4s.requests.bulk.BulkResponse
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import com.sksamuel.elastic4s.requests.searches.{ CompletionSuggestionOption, SearchResponse }
import config.CompletionCustomSuggestion
import models.{ AuthorCompletion, AuthorSearchResponse, CompletionResponseType, QuoteWithAuthor }
import play.api.libs.json.{ JsResult, Json }
import play.api.mvc.Result
import play.api.mvc.Results.Ok

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

object EsResponseHandler extends ResponseError {

  def validateJson[T](jsResult: JsResult[T]): Future[T] = {
    if (jsResult.isSuccess) Future.successful(jsResult.get)
    else Future.failed(new NoSuchElementException("Json parse error"))
  }

  def responseEsResult[T](response: Response[T])(implicit indexName: String): Result = {
    response.result match {
      case bulkResponse: BulkResponse =>
        checkResponse(response, bulkResponse.hasSuccesses)
      case deleteIndexResponse: DeleteIndexResponse =>
        checkResponse(response, deleteIndexResponse.acknowledged)
      case searchResponse: SearchResponse if searchResponse.suggestions.isEmpty =>
        sendSearchedResponse(searchResponse, searchResponse.nonEmpty)
      case searchResponse: SearchResponse =>
        getCompletionAuthor(searchResponse)
      case _ =>
        checkResponse(response, isResponseResult = true)

    }
  }

  private def sendSearchedResponse(
    response: SearchResponse,
    isResponseResult: Boolean
  )(implicit indexName: String): Result = {
    if (isResponseResult) {
      log.info(s"Total hits for the searched text: ${ response.size }")
      quoteAuthorSearchedResponse(response)
    } else {
      notFound(EsPlaceHolder(EmptyDbMsg.msg))
    }
  }

  /*
    Either Search Response is the list of QuoteWithAuthor that is the response when the user search for the full quote
      using the quote as an input parameter for search
    Else it will be the list of AuthorSearchResponse that is the response when the user search for the author and it
      matched the response using prefix match ES API instead os using any Search Suggestion API
   */
  private def quoteAuthorSearchedResponse(response: SearchResponse): Result = {
    val tryQuotesWithAuthor: Seq[Try[QuoteWithAuthor]] = response.safeTo[QuoteWithAuthor].toList
    Try(tryQuotesWithAuthor.map(_.get)) match {
      case Success(quotesWithAuthor) => Ok(Json.toJson(quotesWithAuthor))
      case Failure(_) =>
        val authorSearchResponse = response.to[AuthorSearchResponse].toList
        Ok(
          Json.toJson(
            AuthorCompletion(
              CompletionResponseType.PrefixMatchCompletion,
              authorSearchResponse.map(_.quoteDetails.author).distinct
            )
          )
        )
    }
  }

  private def checkResponse[T](
    response: Response[T],
    isResponseResult: Boolean
  )(implicit indexName: String): Result = {
    if (response.isSuccess && isResponseResult) {
      Ok(s"Success on performing action on index: $indexName")
    } else {
      log.error(
        s"Error while performing action on index: $indexName with an error: ${ response.error.reason }"
      )
      notFound(EsPlaceHolder(response.error.reason))
    }
  }

  private def getCompletionAuthor(response: SearchResponse): Result = {
    log.info(s"Getting list of the author auto completion list")

    val searchResponse: Seq[CompletionSuggestionOption] =
      response
        .suggestions(CompletionCustomSuggestion.suggestionName)
        .flatMap(_.toCompletion.options)

    if (searchResponse.isEmpty) notFound(EsPlaceHolder("Searched author not found"))
    else {
      val results: Seq[String] = searchResponse.map(_.text)
      Ok(Json.toJson(AuthorCompletion(CompletionResponseType.AutoCompletion, results)))
    }
  }

}
