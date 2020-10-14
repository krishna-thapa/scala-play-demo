package auth.form

import auth.model.LoginDetails
import play.api.data.Forms._
import play.api.data.{ Form, Forms }

object LoginForm {

  val loginForm: Form[LoginDetails] = Form {
    mapping(
      "username" -> nonEmptyText,
      "password" -> text(minLength = 3)  /// requires a minimum of three characters
    )(LoginDetails.apply)(LoginDetails.unapply)
  }
}
