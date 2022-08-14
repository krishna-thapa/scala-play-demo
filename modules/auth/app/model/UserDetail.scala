package model

import com.krishna.model.base.UserIdResource
import play.api.libs.json.{ Json, OFormat }

import java.sql.Date
import java.util.UUID

case class UserDetail(
  userId: UUID,
  name: String,
  email: String,
  joinedDate: Date,
  isAdmin: Boolean
) extends UserIdResource

object UserDetail {
  implicit lazy val userListFormat: OFormat[UserDetail] = Json.format[UserDetail]

  def apply(userInfo: UserInfo): UserDetail =
    new UserDetail(
      userInfo.userId,
      s"${ userInfo.firstName } ${ userInfo.lastName }",
      userInfo.email,
      userInfo.createdDate,
      userInfo.isAdmin
    )

}
