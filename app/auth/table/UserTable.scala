package auth.table

import java.sql.Date

import auth.model.SignUp
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

class UserTable(tag: Tag) extends Table[SignUp](tag, "user_details_table") {

  def id: Rep[Int]           = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def firstName: Rep[String] = column[String]("first_name")
  def lastName: Rep[String]  = column[String]("last_name")
  def email: Rep[String]     = column[String]("email")
  def password: Rep[String]  = column[String]("password")
  def createdDate: Rep[Date] = column[Date]("created_date")
  def isAdmin: Rep[Boolean]  = column[Boolean]("is_admin")
  def * : ProvenShape[SignUp] =
    (id, firstName, lastName, email, password, createdDate, isAdmin) <>
      ((SignUp.apply _).tupled, SignUp.unapply)
}

object UserTable {
  val userTableQueries = TableQuery[UserTable]
}
