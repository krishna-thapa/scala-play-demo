package model

import com.krishna.model.auth.ProfilePictureInfo
import com.krishna.model.base.UserIdResource
import play.api.libs.json.{ Json, OFormat }

import java.sql.Date
import java.util.UUID

case class UserInfo(
  userId: UUID,
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  createdDate: Date,
  isAdmin: Boolean = false,
  profilePictureInfo: Option[ProfilePictureInfo],
  profilePicture: Array[Byte]
) extends Login
    with UserIdResource

object UserInfo {
  implicit lazy val singUpFormat: OFormat[UserInfo] = Json.format[UserInfo]
}
