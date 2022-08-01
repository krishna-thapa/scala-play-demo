package com.krishna.response

import com.krishna.response.ErrorMsg.EsErrorMessage
import com.krishna.util.Logging
import com.krishna.util.UtilImplicits.ToFuture
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.concurrent.Future

trait ResponseError extends Logging {

  def badRequest(msg: String): Future[Result] = {
    log.error(s"Bad request error: $msg")
    BadRequest(Json.toJson(ResponseErrorMsg(msg))).toFuture
  }

  def invalidForm[T](formWithErrors: Form[T], errorMsg: ErrorMsg): Future[Result] = {
    log.error(s"Invalid request form format: $formWithErrors")
    UnprocessableEntity(Json.toJson(ResponseErrorMsg(errorMsg.msg))).toFuture
  }

  def badGateway(msg: String): Future[Result] = {
    log.error(s"Bad Gateway from server error: $msg")
    BadGateway(Json.toJson(ResponseErrorMsg(msg))).toFuture
  }

  def notFound(errorMsg: ErrorMsg): Future[Result] = {
    log.error(s"Not Found error: ${ errorMsg.msg }")
    NotFound(Json.toJson(ResponseErrorMsg(errorMsg.msg))).toFuture
  }

  def internalServerError(msg: String): Future[Result] = {
    log.error(s"Internal server error: $msg")
    InternalServerError(Json.toJson(ResponseErrorMsg(msg))).toFuture
  }

  def unauthorized(errorMsg: ErrorMsg): Future[Result] = {
    log.error(s"Unauthorized with reason: ${ errorMsg.msg }")
    Unauthorized(Json.toJson(ResponseErrorMsg(errorMsg.msg))).toFuture
  }

  def forbidden(errorMsg: ErrorMsg): Future[Result] = {
    log.error(s"Forbidden request: ${ errorMsg.msg }")
    Forbidden(Json.toJson(ResponseErrorMsg(errorMsg.msg))).toFuture
  }

  def notAcceptable(msg: String): Future[Result] = {
    val errorMessage: String = s"Account already exist with: $msg"
    log.error(errorMessage)
    NotAcceptable(Json.toJson(ResponseErrorMsg(errorMessage))).toFuture
  }

  def bcryptValidationFailed(errorMsg: ErrorMsg): Future[Result] = {
    log.error(errorMsg.msg)
    FailedDependency(Json.toJson(ResponseErrorMsg(errorMsg.msg))).toFuture
  }

  def elasticSearchNotFound(errorMsg: EsErrorMessage): Result = {
    log.error(s"Error on elastic search module: ${ errorMsg.msg }")
    NotFound(Json.toJson(ResponseErrorMsg(errorMsg.msg)))
  }

}
