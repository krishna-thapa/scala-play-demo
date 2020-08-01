package controllers

import play.api.libs.json.{ Json, Writes }
import play.api.mvc.Result
import play.api.mvc.Results._
import utils.Logging

trait Responses extends Logging {

  def badRequest(msg: String): Result = {
    log.warn(s"Bad Request error: $msg")
    BadRequest(Json.toJson(Response(msg)))
  }

  def notFound(msg: String): Result = {
    log.warn(s"Not Found error: $msg")
    NotFound(Json.toJson(Response(msg)))
  }
}

case class Response(userMsg: String)

object Response {
  implicit val responseError: Writes[Response] =
    Json.writes[Response]
}
