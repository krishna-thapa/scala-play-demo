package com.krishna.response

import com.krishna.util.Logging
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.{
  BadGateway,
  BadRequest,
  FailedDependency,
  Forbidden,
  InternalServerError,
  NotAcceptable,
  NotFound,
  Unauthorized
}

trait ResponseError extends Logging {

  def badRequest(msg: String): Result = {
    log.error(s"Bad request error: $msg")
    BadRequest(Json.toJson(ResponseErrorMsg(msg)))
  }

  def badGateway(msg: String): Result = {
    log.error(s"Bad Gateway from server error: $msg")
    BadGateway(Json.toJson(ResponseErrorMsg(msg)))
  }

  def notFound(msg: String): Result = {
    log.warn(s"Not Found error: $msg")
    NotFound(Json.toJson(ResponseErrorMsg(msg)))
  }

  def internalServerError(msg: String): Result = {
    log.error(s"Internal server error: $msg")
    InternalServerError(Json.toJson(ResponseErrorMsg(msg)))
  }

  def unauthorized(msg: String): Result = {
    log.error(s"Unauthorized with reason: $msg")
    Unauthorized(Json.toJson(ResponseErrorMsg(msg)))
  }

  def forbidden(msg: String): Result = {
    log.error(s"Forbidden request: $msg")
    Forbidden(Json.toJson(ResponseErrorMsg(msg)))
  }

  def notAcceptable(msg: String): Result = {
    val errorMessage: String = s"Account already exist with: $msg"
    log.error(errorMessage)
    NotAcceptable(Json.toJson(ResponseErrorMsg(errorMessage)))
  }

  def bcryptValidationFailed(msg: String): Result = {
    val errorMessage: String = s"Bcrypt encryption failed: $msg"
    log.error(errorMessage)
    FailedDependency(Json.toJson(ResponseErrorMsg(errorMessage)))
  }

}
