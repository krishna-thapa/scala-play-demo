package auth.form

import auth.model.LoginDetail

case class SignUpForm(
    firstName: String,
    lastName: String,
    email: String,
    password: String
) extends LoginDetail
