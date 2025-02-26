package com.krishna.util

import com.krishna.model.exception.ParseException
import play.api.libs.json.{ JsError, JsSuccess, Json, Reads }

object Parser extends Logging {

  def parseJson[T](json: String)(implicit reads: Reads[T]): T = {
    Json.parse(json).validate[T] match {
      case JsSuccess(result, _) => result
      case JsError(errors) =>
        log.error(s"Encountered errors parsing $json: $errors")
        throw ParseException("Parsing message error")
    }
  }

}
