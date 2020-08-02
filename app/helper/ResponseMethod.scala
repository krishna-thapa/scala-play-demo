package helper

import controllers.Assets.Ok
import models.{ CustomQuotesQuery, QuotesTable }
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{ Json, Writes }
import play.api.mvc.Result
import play.api.mvc.Results._
import utils.Logging

trait ResponseMethod extends Logging {

  def badRequest(msg: String): Result = {
    log.warn(s"Bad request error: $msg")
    BadRequest(Json.toJson(Response(msg)))
  }

  def notFound(msg: String): Result = {
    log.warn(s"Not Found error: $msg")
    NotFound(Json.toJson(Response(msg)))
  }

  def internalServerError(msg: String): Result = {
    log.warn(s"Internal server error: $msg")
    InternalServerError(Json.toJson(Response(msg)))
  }

  case class Response(userMsg: String)

  object Response {
    implicit val responseError: Writes[Response] =
      Json.writes[Response]
  }

  // update the upper bound with more generic subtype
  def responseRedsult[T <: CustomQuotesQuery](records: Seq[T]): Result = {
    if (records.nonEmpty) Ok(Json.toJson(records))
    else notFound("Database is empty!")
  }

}
