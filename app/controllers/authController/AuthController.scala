package controllers.authController

import java.time.Clock

import auth.form.AuthForms
import auth.model.UserToken
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

  implicit val clock: Clock = Clock.systemUTC

  def signIn: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    log.info("Executing signIn Controller")
    // Add request validation
    val signInResult = AuthForms.signInForm.bindFromRequest.fold(
      formWithErrors => {
        badRequest(s"The form was not in the expected format: $formWithErrors")
      },
      signInDetails => {
        // Need to check if the user has enter wrong password but has an account already
        if (authDAO.isAccountExist(signInDetails.email)) {
          authDAO.isValidLogin(signInDetails) match {
            case Some(validUser) =>
              log.info("Success on authentication!")
              Ok.addingToJwtSession("user", UserToken(validUser.email))
            case None => unauthorized(s"Wrong password for: ${signInDetails.email}")
          }
        } else notFound(s"User account is not found for : ${signInDetails.email}")
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
        if (!authDAO.isAccountExist(signUpDetails.email))
          Ok(Json.toJson(authDAO.signUpUser(signUpDetails)))
        else notAcceptable(s"${signUpDetails.email}")
      }
    )

  }

}
