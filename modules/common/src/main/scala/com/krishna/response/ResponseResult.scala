package com.krishna.response

import com.krishna.model.base.IdResource
import com.krishna.response.ErrorMsg._
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
    else notFound(EmptyDbMsg)
  }

  // TODO have to remove it
  def responseSeqString(records: Seq[String]): Result = {
    if (records.nonEmpty) Ok(Json.toJson(records))
    else notFound(EmptyDbMsg)
  }

  def responseOptionResult[T <: IdResource](
      record: Option[T]
  )(implicit conv: OFormat[T]): Result = {
    record match {
      case Some(quote) => Ok(Json.toJson(quote))
      case None =>
        notFound(EmptyDbMsg)
    }
  }

  def responseTryResult[T <: IdResource](record: Try[T])(implicit conv: OFormat[T]): Result = {
    record match {
      case Success(quote)     => Ok(Json.toJson(quote))
      case Failure(exception) => internalServerError(exception.getMessage)
    }
  }

  def responseEitherResult[T <: IdResource](
      record: Either[ErrorMsg, T]
  )(implicit conv: OFormat[T]): Result = {
    record match {
      case Left(errorMsg) =>
        responseErrorResult(errorMsg)
      case Right(quote) => Ok(Json.toJson(quote))
    }
  }

  // Use this method on each response error
  def responseErrorResult(errorMsg: ErrorMsg): Result = {
    errorMsg match {
      case EmptyDbMsg                                     => notFound(EmptyDbMsg)
      case invalidDate: InvalidDate                       => badRequest(invalidDate.msg)
      case invalidCsvId: InvalidCsvId                     => badRequest(invalidCsvId.msg)
      case NoAuthorizationField                           => badGateway(NoAuthorizationField.msg)
      case tokenDecodeFailure: TokenDecodeFailure         => badGateway(tokenDecodeFailure.msg)
      case AuthenticationFailed                           => unauthorized(AuthenticationFailed)
      case authorizationForbidden: AuthorizationForbidden => forbidden(authorizationForbidden.email)
      case accountNotFound: AccountNotFound               => notFound(accountNotFound)
      case _                                              => badRequest("Something went wrong")
    }
  }
}
