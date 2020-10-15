package auth.model

import java.sql.Date

import play.api.libs.json.{ Json, OFormat }

case class SignUp(
    id: Int,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    createdDate: Date,
    isAdmin: Boolean = false
) extends LoginDetail

object SignUp {
  implicit lazy val singUpFormat: OFormat[SignUp] = Json.format[SignUp]
}
