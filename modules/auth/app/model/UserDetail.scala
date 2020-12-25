package model

import com.krishna.model.base.IdResource

import java.sql.Date
import play.api.libs.json.{ Json, OFormat }

case class UserDetail(
    id: Int,
    name: String,
    email: String,
    joinedDate: Date,
    isAdmin: Boolean
) extends IdResource

object UserDetail {
  implicit lazy val userListFormat: OFormat[UserDetail] = Json.format[UserDetail]

  def apply(userInfo: UserInfo): UserDetail =
    new UserDetail(
      userInfo.id,
      s"${userInfo.firstName} ${userInfo.lastName}",
      userInfo.email,
      userInfo.createdDate,
      userInfo.isAdmin
    )
}
