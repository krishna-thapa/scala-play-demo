package auth.model

import play.api.libs.json._

case class UserToken(name: String)

object UserToken {
  implicit lazy val userFormat: OFormat[UserToken] = Json.format[UserToken]
}
