package model

import java.sql.Date

import play.api.libs.json._

case class UserToken(
    email: String,
    fullName: String,
    isAdmin: Boolean,
    joinedDate: Date
)

object UserToken {
  implicit lazy val userFormat: OFormat[UserToken] = Json.format[UserToken]
}
