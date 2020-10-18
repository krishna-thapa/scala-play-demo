package auth.controller

import java.time.Clock

import auth.dao.AuthDAO
import auth.depInject.{ SecuredController, SecuredControllerComponents }
import auth.form.AuthForms
import auth.model.{ UserList, UserToken }
import javax.inject.{ Inject, Singleton }
import pdi.jwt.JwtSession._
import play.api.Configuration
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat
import response.ResponseResult
import utils.Logging
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

@Singleton
class AuthController @Inject()(
    scc: SecuredControllerComponents,
    authDAO: AuthDAO
)(implicit executionContext: ExecutionContext, config: Configuration)
    extends SecuredController(scc)
    with Logging
    with ResponseResult {

  implicit val clock: Clock = Clock.systemUTC

  // Regex to validate the email pattern
  def isEmailValid(email: String): Boolean =
    if ("""(?=[^\s]+)(?=(\w+)@([\w.]+))""".r.findFirstIn(email).isEmpty) false else true

  /**
    * Sign In the existing user
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
            case Right(value)    => responseOk(value)
            case Left(exception) => bcryptValidationFailed(exception.getMessage)
          }
        } else notAcceptable(s"${signUpDetails.email}")
      }
    )
  }

  /**
    * Get all the existing users from the database: Only the Admin can
    * @return Seq of users
    */
  def getAllUser: Action[AnyContent] = UserAction { implicit request =>
    log.info("Executing getAllUser Controller")
    responseSeqResult(authDAO.listAllUser())
  }

  /**
    * Alter the admin role to the selected user: Only the Admin can
    * @param email to select the user's account
    * @return Record id or an exception
    */
  def toggleAdminRole(email: String): Action[AnyContent] = Action { implicit request =>
    log.info("Executing toggleAdminRole Controller")
    val toggleAdmin = (email: String) => authDAO.toggleAdmin(email)
    runApiAction(email)(toggleAdmin)
  }

  /**
    * Remove the user account from the database: Only the Admin can
    * @param email to select the user's account
    * @return Record id or an exception
    */
  def removeUser(email: String): Action[AnyContent] = AdminAction { implicit request =>
    log.info("Executing removeUser Controller")
    val removeAccount = (email: String) => authDAO.removeUserAccount(email)
    runApiAction(email)(removeAccount)
  }

  /**
    * Get the user info from selected email: only the logged in user can
    * @param email to select the user's account
    * @return Record details or an exception
    */
  def getUserInfo(email: String): Action[AnyContent] = UserAction { implicit request =>
    log.info("Executing getUserInfo Controller")
    val getUserInfoDetails = (email: String) => authDAO.userAccount(email)
    runApiAction(email)(getUserInfoDetails)
  }

  /**
    * Update the user info details, different email will replace the older email. Only logged in user can do
    * @param id to select the user's account
    * @return User Info once the success update on the record or an error response
    */
  def updateUserInfo(id: Int): Action[AnyContent] = Action { implicit request =>
    log.info("Executing updateUserInfo Controller")
    // Add request validation
    AuthForms.signUpForm.bindFromRequest.fold(
      formWithErrors => {
        badRequest(s"The form was not in the expected format: $formWithErrors")
      },
      userDetails => {
        authDAO.updateUserInfo(id, userDetails) match {
          case Right(user) =>
            user match {
              case Success(_) =>
                responseOk(UserList(authDAO.checkValidEmail(userDetails.email).head))
              case Failure(exception) => badRequest(exception.getMessage)
            }
          case Left(exception) => bcryptValidationFailed(exception.getMessage)
        }
      }
    )
  }

  // sign out

  /**
    * Common method to verify the email and run the called function
    * @param email to select the user account record
    * @param fun function to be called upon the selected record
    * @return Result of the API response
    */
  def runApiAction[T](
      email: String
  )(fun: String => Either[Result, T])(implicit conv: OFormat[T]): Result = {
    log.info(s"Checking the format of an email: $email")
    if (isEmailValid(email)) {
      fun(email) match {
        // TODO Might want to response with the user details instead of a success id
        case Right(value)    => responseOk(value)
        case Left(exception) => exception
      }
    } else badRequest(s"Email is in wrong format: $email")
  }
}
