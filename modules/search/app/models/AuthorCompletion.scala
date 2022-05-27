package models

import models.CompletionResponseType.CompletionResponseType
import play.api.libs.json.{ Json, OFormat }

object CompletionResponseType extends Enumeration {
  type CompletionResponseType = Value
  val AutoCompletion, PrefixMatchCompletion = Value

  implicit val format = Json.formatEnum(this)
}

case class AuthorCompletion(
  responseType: CompletionResponseType,
  responseList: Seq[String]
)

object AuthorCompletion {
  implicit lazy val AuthorCompletion: OFormat[AuthorCompletion] = Json.format[AuthorCompletion]
}
