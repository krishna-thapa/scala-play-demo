package com.krishna.response

import com.krishna.model.base.IdResource
import com.krishna.response.ResponseMsg.{ EmptyDbMsg, InvalidDate }
import com.sksamuel.elastic4s.Response
import play.api.libs.json.{ Json, OFormat }
import play.api.mvc.Result
import play.api.mvc.Results.Ok

import scala.util.{ Failure, Success, Try }

trait ResponseResult extends ResponseError {

  def responseOk[T](result: T)(implicit conv: OFormat[T]): Result = {
    Ok(Json.toJson(result))
  }

  def responseSeqResult[T <: IdResource](records: Seq[T])(implicit conv: OFormat[T]): Result = {
    if (records.nonEmpty) Ok(Json.toJson(records))
    else notFound(EmptyDbMsg.msg)
  }

  // TODO have to remove it
  def responseSeqString(records: Seq[String]): Result = {
    if (records.nonEmpty) Ok(Json.toJson(records))
    else notFound(EmptyDbMsg.msg)
  }

  def responseOptionResult[T <: IdResource](
      record: Option[T]
  )(implicit conv: OFormat[T]): Result = {
    record match {
      case Some(quote) => Ok(Json.toJson(quote))
      case None =>
        notFound(EmptyDbMsg.msg)
    }
  }

  def responseTryResult[T <: IdResource](record: Try[T])(implicit conv: OFormat[T]): Result = {
    record match {
      case Success(quote)     => Ok(Json.toJson(quote))
      case Failure(exception) => internalServerError(exception.getMessage)
    }
  }

  def responseEitherResult[T <: IdResource](
      record: Either[ResponseMsg, T]
  )(implicit conv: OFormat[T]): Result = {
    record match {
      case Left(errorMsg) =>
        errorMsg match {
          case EmptyDbMsg               => notFound(EmptyDbMsg.msg)
          case invalidDate: InvalidDate => badRequest(invalidDate.msg)
        }
      case Right(quote) => Ok(Json.toJson(quote))
    }
  }

  def responseEsResult[T](response: Response[T]): Result = {
    if (response.isSuccess) {
      Ok(s"Success: ${response.body}")
    } else {
      log.error(s"Error while deleting an index: ${response.error.reason}")
      notFound(s"${response.error.reason}")
    }
  }

  def responseEsSeqResult[T](responses: Seq[Response[T]]): Result = {
    val headResponse: Response[T] = responses.head
    if (headResponse.isSuccess) {
      Ok(s"Success with response code: ${responses.head.status}")
    } else {
      log.error(s"Error while writing on index: ${headResponse.error.reason}")
      notFound(s"${headResponse.error.reason}")
    }
  }

}
