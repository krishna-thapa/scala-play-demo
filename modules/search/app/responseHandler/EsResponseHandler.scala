package responseHandler

import com.krishna.response.ErrorMsg.{ EmptyDbMsg, EsPlaceHolder }
import com.krishna.response.ResponseError
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.playjson.playJsonHitReader
import com.sksamuel.elastic4s.requests.bulk.BulkResponse
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import com.sksamuel.elastic4s.requests.searches.{ CompletionSuggestionOption, SearchResponse }
import config.SuggestionName
import models.{ AuthorCompletion, CompletionResponseType, QuoteWithAuthor }
import play.api.libs.json.{ JsResult, Json }
import play.api.mvc.Result
import play.api.mvc.Results.{ NotFound, Ok }

import scala.concurrent.Future

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
      log.info(s"Total hits for the searched text: ${response.size}")
      val records = response.to[QuoteWithAuthor].toList
      Ok(Json.toJson(records))
    } else {
      notFound(EsPlaceHolder(EmptyDbMsg.msg))
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
        s"Error while performing action on index: $indexName with an error: ${response.error.reason}"
      )
      notFound(EsPlaceHolder(response.error.reason))
    }
  }

  private def getCompletionAuthor(response: SearchResponse): Result = {
    log.info(s"Getting list of the author auto completion list")

    val searchResponse: Seq[CompletionSuggestionOption] =
      response
        .suggestions(SuggestionName.completionAuthor.toString)
        .flatMap(_.toCompletion.options)

    if (searchResponse.isEmpty) NotFound("Searched author not found")
    else {
      // Need to sort in FE code as per input text
      val results: Seq[String] = searchResponse.map(_.text)
      Ok(Json.toJson(AuthorCompletion(CompletionResponseType.AutoCompletion, results)))
    }
  }
}
