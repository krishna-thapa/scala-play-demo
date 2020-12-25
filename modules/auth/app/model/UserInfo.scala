package model

import com.krishna.model.base.IdResource

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
    with IdResource

object UserInfo {
  implicit lazy val singUpFormat: OFormat[UserInfo] = Json.format[UserInfo]
}
