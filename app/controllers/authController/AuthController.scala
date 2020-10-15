package controllers.authController

import java.time.Clock

import auth.form.AuthForms
import auth.model.UserDetail
import daos.AuthDAO
import javax.inject.{ Inject, Singleton }
import pdi.jwt.JwtSession._
import play.api.Configuration
import play.api.mvc._
import play.api.libs.json.Json
import response.ResponseMethod
import utils.Logging

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class AuthController @Inject()(
    cc: ControllerComponents,
    authDAO: AuthDAO
)(implicit executionContext: ExecutionContext, config: Configuration)
    extends AbstractController(cc)
    with Logging
    with ResponseMethod {

  private val passwords: Seq[String] = Seq("foo", "poo")

  implicit val clock: Clock = Clock.systemUTC

  def signIn: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    log.info("Executing signIn Controller")
    // Add request validation
    val signInResult = AuthForms.signInForm.bindFromRequest.fold(
      formWithErrors => {
        badRequest(s"The form was not in the expected format: $formWithErrors")
      },
      signInDetails => {
        if (passwords.contains(signInDetails.password)) {
          log.info("Success on authentication!")
          Ok.addingToJwtSession("user", UserDetail(signInDetails.email))
        } else
          unauthorized(s"Unauthorized error for user: ${signInDetails.email}")
      }
    )
    Future(signInResult)
  }

  def signUp: Action[AnyContent] = Action { implicit request =>
    log.info("Executing signUp Controller")
    // Add request validation
    AuthForms.signUpForm.bindFromRequest.fold(
      formWithErrors => {
        badRequest(s"The form was not in the expected format: $formWithErrors")
      },
      signUpDetails => {
        // need to check if the account already exist
        Ok(Json.toJson(authDAO.signUpUser(signUpDetails)))
      }
    )

  }

}
