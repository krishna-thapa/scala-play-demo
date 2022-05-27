package com.krishna.response

import play.api.libs.json.{ Json, OFormat }

case class OkResponse(success: String)

object OkResponse {

  implicit lazy val okResponseFormat: OFormat[OkResponse] =
    Json.format[OkResponse]

}
