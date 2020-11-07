package form

import model.Login

case class SignInForm(
    email: String,
    password: String
) extends Login
