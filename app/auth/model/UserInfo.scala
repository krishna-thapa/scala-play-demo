package auth.model

import java.sql.Date

import play.api.libs.json.{ Json, OFormat }

case class UserInfo(
    id: Int,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    createdDate: Date,
    isAdmin: Boolean = false
) extends Login

object UserInfo {
  implicit lazy val singUpFormat: OFormat[UserInfo] = Json.format[UserInfo]
}
