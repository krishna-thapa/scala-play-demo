package form

import play.api.data.Forms._
import play.api.data.Form

object AuthForms {

  val signInForm: Form[SignInForm] = Form {
    mapping(
      "email"    -> email,
      "password" -> text(minLength = 3) // requires a minimum of three characters
    )(SignInForm.apply)(SignInForm.unapply)
  }

  val signUpForm: Form[SignUpForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName"  -> nonEmptyText,
      "email"     -> email,
      "password"  -> text(minLength = 3) // requires a minimum of three characters
    )(SignUpForm.apply)(SignUpForm.unapply)
  }
}
