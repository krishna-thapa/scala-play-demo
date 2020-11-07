package form

import model.Login

case class SignUpForm(
    firstName: String,
    lastName: String,
    email: String,
    password: String
) extends Login
