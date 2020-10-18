package response

import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.{ NotAcceptable, _ }
import utils.Logging

trait ResponseError extends Logging {

  def badRequest(msg: String): Result = {
    log.error(s"Bad request error: $msg")
    BadRequest(Json.toJson(ResponseErrorMsg(msg)))
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
    val errorMessage: String = s"Unauthorized with reason: $msg"
    log.error(errorMessage)
    Unauthorized(Json.toJson(ResponseErrorMsg(errorMessage)))
  }

  def forbidden(msg: String): Result = {
    val errorMessage: String = s"Forbidden for user: $msg"
    log.error(errorMessage)
    Forbidden(Json.toJson(ResponseErrorMsg(errorMessage)))
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
