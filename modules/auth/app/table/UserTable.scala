package table

import com.krishna.db.QuotesPostgresDriver.api._
import com.krishna.model.auth.ProfilePictureInfo
import model.UserInfo
import slick.lifted.ProvenShape

import java.sql.Date
import java.util.UUID

class UserTable(tag: Tag) extends Table[UserInfo](tag, "user_details_table") {

  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def firstName: Rep[String] = column[String]("first_name")
  def lastName: Rep[String] = column[String]("last_name")
  def email: Rep[String] = column[String]("email")
  def password: Rep[String] = column[String]("password")
  def createdDate: Rep[Date] = column[Date]("created_date")
  def isAdmin: Rep[Boolean] = column[Boolean]("is_admin")

  def profilePictureInfo: Rep[Option[ProfilePictureInfo]] =
    column[Option[ProfilePictureInfo]]("profile_picture_info")

  def profilePicture: Rep[Array[Byte]] = column[Array[Byte]]("profile_picture")

  def * : ProvenShape[UserInfo] =
    (
      id,
      firstName,
      lastName,
      email,
      password,
      createdDate,
      isAdmin,
      profilePictureInfo,
      profilePicture
    ) <>
      ((UserInfo.apply _).tupled, UserInfo.unapply)

}

object UserTable {
  val userTableQueries = TableQuery[UserTable]
}
