package auth.form

import auth.model.Login

case class SignInForm(
    email: String,
    password: String
) extends Login
