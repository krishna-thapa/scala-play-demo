package auth.form

import auth.model.LoginDetail

case class SignInForm(
    email: String,
    password: String
) extends LoginDetail
