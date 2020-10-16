package auth.controller

import java.time.Clock

import auth.dao.AuthDAO
import auth.form.AuthForms
import auth.model.UserToken
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

  // Regex to validate the email pattern
  def isEmailValid(email: String): Boolean =
    if ("""(?=[^\s]+)(?=(\w+)@([\w.]+))""".r.findFirstIn(email).isEmpty) false else true

  /**
    * Sing In the existing user
    * @return Auth JWT token in the header if success
    */
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
            case Right(validUser) =>
              log.info("Success on authentication!")
              Ok.addingToJwtSession(
                "user",
                UserToken(
                  validUser.email,
                  s"${validUser.firstName.capitalize} ${validUser.lastName.capitalize}",
                  validUser.isAdmin,
                  validUser.createdDate
                )
              )
            case Left(exceptionResult) => exceptionResult
          }
        } else notFound(s"User account is not found for : ${signInDetails.email}")
      }
    )
    Future(signInResult)
  }

  /**
    * Sign up the new account in the database
    * @return Record id or an exception
    */
  def signUp: Action[AnyContent] = Action { implicit request =>
    log.info("Executing signUp Controller")
    // Add request validation
    AuthForms.signUpForm.bindFromRequest.fold(
      formWithErrors => {
        badRequest(s"The form was not in the expected format: $formWithErrors")
      },
      signUpDetails => {
        // need to check if the account already exist
        if (!authDAO.isAccountExist(signUpDetails.email)) {
          authDAO.signUpUser(signUpDetails) match {
            case Right(value)    => Ok(Json.toJson(value))
            case Left(exception) => bcryptValidationFailed(exception.getMessage)
          }
        } else notAcceptable(s"${signUpDetails.email}")
      }
    )
  }

  /**
    * Get all the existing users from the database
    * @return Seq of users
    */
  def getAllUser: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getAllUser Controller")
    responseSeqResult(authDAO.listAllUser())
  }

  /**
    * Alter the admin role to the selected user
    * @param email to select the user's account
    * @return Record id or an exception
    */
  def toggleAdminRole(email: String): Action[AnyContent] = Action { implicit request =>
    log.info("Executing toggleAdminRole Controller")
    if (isEmailValid(email)) {
      authDAO.toggleAdmin(email) match {
        // TODO Might want to response with the user details instead of a success id
        case Right(value)    => Ok(Json.toJson(value))
        case Left(exception) => exception
      }
    } else badRequest(s"Email is in wrong format: $email")
  }

  /**
    * Remove the user account from the database
    * @param email to select the user's account
    * @return Record id or an exception
    */
  def removeUser(email: String): Action[AnyContent] = Action { implicit request =>
    log.info("Executing removeUser Controller")
    if (isEmailValid(email))
      authDAO.removeUserAccount(email) match {
        case Right(value)    => Ok(Json.toJson(value))
        case Left(exception) => exception
      } else badRequest(s"Email in wrong format: $email")
  }

  // get the user info from selected email: only the logged in user can

  // update the user info: Only the logged in user can

  // sign out
}
