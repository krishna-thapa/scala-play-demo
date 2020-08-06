package helper

import play.api.libs.json.{ Json, Writes }

sealed trait Response

case class ResponseErrorMsg(userMsg: String) extends Response

object ResponseErrorMsg {
  implicit val responseError: Writes[ResponseErrorMsg] =
    Json.writes[ResponseErrorMsg]
}
