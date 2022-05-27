package com.krishna.util

import play.api.libs.json.{ JsError, JsSuccess, Json, Reads }

object Parser extends Logging {

  def parseJson[T](json: String)(implicit reads: Reads[T]): T = {
    Json.parse(json).validate[T] match {
      case JsSuccess(result, _) => result
      case JsError(errors) =>
        log.error(s"Encountered errors parsing $json: $errors")
        throw new Exception("Parsing message error")
    }
  }

}
