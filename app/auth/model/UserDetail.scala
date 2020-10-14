package auth.model

import play.api.libs.json._

case class UserDetail(name: String)

object UserDetail {
  implicit lazy val userFormat: OFormat[UserDetail] = Json.format[UserDetail]
}
