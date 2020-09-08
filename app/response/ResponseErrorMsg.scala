package response

import play.api.libs.json.{ Json, Writes }

case class ResponseErrorMsg(userMsg: String)

object ResponseErrorMsg {
  implicit val responseError: Writes[ResponseErrorMsg] =
    Json.writes[ResponseErrorMsg]
}
