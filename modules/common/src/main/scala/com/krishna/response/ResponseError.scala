package com.krishna.response

import com.krishna.util.Logging
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results._

trait ResponseError extends Logging {

  def badRequest(msg: String): Result = {
    log.error(s"Bad request error: $msg")
    BadRequest(Json.toJson(ResponseErrorMsg(msg)))
  }

  def invalidForm[T](formWithErrors: Form[T], errorMsg: ErrorMsg): Result = {
    log.error(s"Invalid request form format: $formWithErrors")
    UnprocessableEntity(Json.toJson(ResponseErrorMsg(errorMsg.msg)))
  }

  def badGateway(msg: String): Result = {
    log.error(s"Bad Gateway from server error: $msg")
    BadGateway(Json.toJson(ResponseErrorMsg(msg)))
  }

  def notFound(errorMsg: ErrorMsg): Result = {
    log.error(s"Not Found error: ${errorMsg.msg}")
    NotFound(Json.toJson(ResponseErrorMsg(errorMsg.msg)))
  }

  def internalServerError(msg: String): Result = {
    log.error(s"Internal server error: $msg")
    InternalServerError(Json.toJson(ResponseErrorMsg(msg)))
  }

  def unauthorized(errorMsg: ErrorMsg): Result = {
    log.error(s"Unauthorized with reason: ${errorMsg.msg}")
    Unauthorized(Json.toJson(ResponseErrorMsg(errorMsg.msg)))
  }

  def forbidden(errorMsg: ErrorMsg): Result = {
    log.error(s"Forbidden request: ${errorMsg.msg}")
    Forbidden(Json.toJson(ResponseErrorMsg(errorMsg.msg)))
  }

  def notAcceptable(msg: String): Result = {
    val errorMessage: String = s"Account already exist with: $msg"
    log.error(errorMessage)
    NotAcceptable(Json.toJson(ResponseErrorMsg(errorMessage)))
  }

  def bcryptValidationFailed(errorMsg: ErrorMsg): Result = {
    log.error(errorMsg.msg)
    FailedDependency(Json.toJson(ResponseErrorMsg(errorMsg.msg)))
  }

}
