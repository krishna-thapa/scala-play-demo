package com.krishna.response

import com.krishna.model.base.IdResource
import com.krishna.response.ErrorMsg._
import play.api.libs.json.{ Json, OFormat }
import play.api.mvc.Result
import play.api.mvc.Results.Ok

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ResponseResult extends ResponseError {

  def responseOk[T](record: T)(implicit conv: OFormat[T]): Future[Result] = {
    Future.successful(Ok(Json.toJson(record)))
  }

  def responseOkAsync[T](futureRecord: Future[T])(implicit conv: OFormat[T]): Future[Result] = {
    futureRecord.map(record => Ok(Json.toJson(record)))
  }

  def responseSeqResult[T <: IdResource](
    records: Seq[T]
  )(implicit conv: OFormat[T]): Future[Result] = {
    if (records.nonEmpty) Future.successful(Ok(Json.toJson(records)))
    else notFound(EmptyDbMsg)
  }

  def responseSeqResultAsync[T <: IdResource](
    futureRecords: Future[Seq[T]]
  )(implicit conv: OFormat[T]): Future[Result] = {
    futureRecords.flatMap(responseSeqResult(_))
  }

  // TODO : Find a better solution
  def responseSeqString(futureRecords: Future[Seq[String]]): Future[Result] = {
    futureRecords.flatMap { records =>
      if (records.nonEmpty) Future.successful(Ok(Json.toJson(records)))
      else notFound(EmptyDbMsg)
    }
  }

  def responseOptionResult[T <: IdResource](
    futureRecord: Future[Option[T]]
  )(implicit conv: OFormat[T]): Future[Result] = {
    futureRecord.flatMap {
      case Some(quote) => responseOk(quote)
      case None        => notFound(EmptyDbMsg)
    }
  }

  def responseEitherResult[T <: IdResource](
    futureRecord: Future[Either[ErrorMsg, T]]
  )(implicit conv: OFormat[T]): Future[Result] = {
    futureRecord.flatMap {
      case Left(errorMsg) => responseErrorResult(errorMsg)
      case Right(quote)   => responseOk(quote)
    }
  }

  // Use this method on each response error
  def responseErrorResult(errorMsg: ErrorMsg): Future[Result] = {
    errorMsg match {
      case EmptyDbMsg                                     => notFound(EmptyDbMsg)
      case invalidDate: InvalidDate                       => badRequest(invalidDate.msg)
      case invalidCsvId: InvalidCsvId                     => badRequest(invalidCsvId.msg)
      case NoAuthorizationField                           => badGateway(NoAuthorizationField.msg)
      case tokenDecodeFailure: TokenDecodeFailure         => badGateway(tokenDecodeFailure.msg)
      case AuthenticationFailed                           => unauthorized(AuthenticationFailed)
      case authorizationForbidden: AuthorizationForbidden => forbidden(authorizationForbidden)
      case accountNotFound: AccountNotFound               => notFound(accountNotFound)
      case _                                              => badRequest("Something went wrong")
    }
  }

}
