package form

import model.Login
import play.api.data.Forms._
import play.api.data.Form

case class SignInForm(
  email: String,
  password: String
) extends Login

case class SignUpForm(
  firstName: String,
  lastName: String,
  email: String,
  password: String
) extends Login

object AuthForms {

  val signInForm: Form[SignInForm] = Form {
    mapping(
      "email" -> email,
      "password" -> text(minLength = 3) // requires a minimum of three characters
    )(SignInForm.apply)(SignInForm.unapply)
  }

  val signUpForm: Form[SignUpForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "password" -> text(minLength = 3) // requires a minimum of three characters
    )(SignUpForm.apply)(SignUpForm.unapply)
  }

}
