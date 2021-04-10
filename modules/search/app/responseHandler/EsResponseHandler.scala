package responseHandler

import com.krishna.model.QuotesQuery
import com.krishna.response.ErrorMsg.{ EmptyDbMsg, EsPlaceHolder }
import com.krishna.response.ResponseError
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.playjson.playJsonHitReader
import com.sksamuel.elastic4s.requests.bulk.BulkResponse
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import play.api.libs.json.{ JsResult, Json }
import play.api.mvc.Result
import play.api.mvc.Results.Ok

import scala.concurrent.{ ExecutionContext, Future }

object EsResponseHandler extends ResponseError {

  def validateJson[T](jsResult: JsResult[T]): Future[T] = {
    if (jsResult.isSuccess) Future.successful(jsResult.get)
    else Future.failed(new NoSuchElementException("Json parse error"))
  }

  // https://docs.scala-lang.org/overviews/core/implicit-classes.html
  implicit class ErrorRecover(futureResult: Future[Result])(
      implicit executionContext: ExecutionContext
  ) {
    def errorRecover: Future[Result] = {
      futureResult.recover {
        case exception =>
          log.error(
            s"Error while performing action on ES: ${exception.getMessage}"
          )
          badRequest(s"${exception.getMessage}")
      }
    }
  }

  def responseEsResult[T](response: Response[T])(implicit indexName: String): Result = {
    response.result match {
      case bulkResponse: BulkResponse =>
        checkResponse(response, bulkResponse.hasSuccesses)
      case deleteIndexResponse: DeleteIndexResponse =>
        checkResponse(response, deleteIndexResponse.acknowledged)
      case searchResponse: SearchResponse =>
        sendResponse(searchResponse, searchResponse.nonEmpty)
      case _ =>
        checkResponse(response, isResponseResult = true)

    }
  }

  private def sendResponse(
      response: SearchResponse,
      isResponseResult: Boolean
  )(implicit indexName: String): Result = {
    if (isResponseResult) {
      log.info(s"Total hits for the searched text: ${response.size}")
      val records = response.to[QuotesQuery].toList
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
}
