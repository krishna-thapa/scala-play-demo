// Auth has to be appended on the controller so that routes file can read
package controllers.auth

import java.time.Clock

import com.krishna.response.ErrorMsg.{ AccountNotFound, InvalidFormFormat, invalidBcryptValidation }
import com.krishna.response.ResponseResult
import com.krishna.util.Logging
import dao.AuthDAO
import depInject.{ SecuredController, SecuredControllerComponents }
import form.{ AuthForms, SignInForm }
import javax.inject.{ Inject, Singleton }
import model.UserDetail
import pdi.jwt.JwtSession.RichResult
import play.api.Configuration
import play.api.libs.json.OFormat
import play.api.mvc._
import config.{ DecodeHeader, JwtKey }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

@Singleton
class AuthController @Inject()(
    scc: SecuredControllerComponents,
    authDAO: AuthDAO
)(implicit executionContext: ExecutionContext, config: Configuration)
    extends SecuredController(scc)
    with Logging
    with ResponseResult
    with JwtKey {

  implicit val clock: Clock = Clock.systemUTC

  // Regex to validate the email pattern
  def isEmailValid(email: String): Boolean =
    if ("""(?=[^\s]+)(?=(\w+)@([\w.]+))""".r.findFirstIn(email).isEmpty) false else true

  /**
    * Sign In the existing user using sing in form
    * @return Auth JWT token in the header if success with response body of user details
    *  Returns response error if the sign in is not success
    */
  def signIn: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    log.info("Executing signIn Controller")
    // Add request validation
    val signInResult = AuthForms.signInForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          invalidForm[SignInForm](formWithErrors, InvalidFormFormat("Login"))
        },
        signInDetails => {
          // Need to check if the user has enter wrong password but has an account already
          if (authDAO.isAccountExist(signInDetails.email)) {
            authDAO.isValidLogin(signInDetails) match {
              case Right(validUser) =>
                val userDetail: UserDetail = UserDetail(validUser)
                log.info(s"Success on authentication for user: ${userDetail.name}")
                responseOk(userDetail).addingToJwtSession(
                  jwtSessionKey,
                  UserDetail(validUser)
                )
              case Left(exceptionResult) => exceptionResult
            }
          } else responseErrorResult(AccountNotFound(signInDetails.email))
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
    AuthForms.signUpForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          badRequest(s"The signup From was not in the expected format: $formWithErrors")
        },
        signUpDetails => {
          // need to check if the account already exist
          if (!authDAO.isAccountExist(signUpDetails.email)) {
            authDAO.signUpUser(signUpDetails) match {
              case Right(value) => responseOk(value)
              case Left(exception) =>
                bcryptValidationFailed(invalidBcryptValidation(exception.getMessage))
            }
          } else notAcceptable(s"${signUpDetails.email}")
        }
      )
  }

  /**
    * List all the users from the database: Only Admin can perform this action
    * @return Seq of users
    */
  def getAllUser: Action[AnyContent] = AdminAction { implicit request =>
    log.info("Executing getAllUser Controller")
    responseSeqResult(authDAO.listAllUser())
  }

  /**
    * Alter the admin role to the selected user: Only the Admin can
    * @param email to select the user's account
    * @return Record id or an exception
    */
  def toggleAdminRole(email: String): Action[AnyContent] = AdminAction { implicit request =>
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
    * Get the user info from selected email: only the Admin or logged in user can
    * Logged in user can only view own record info whereas an admin can view anyone's record
    * @param email to select the user's account
    * @return Record details or an exception
    */
  def getUserInfo(email: String): Action[AnyContent] = UserAction { implicit request =>
    log.info("Executing getUserInfo Controller")

    DecodeHeader(request.headers) match {
      case Right(user) =>
        val overrideEmail      = if (user.isAdmin) email else user.email
        val getUserInfoDetails = (overrideEmail: String) => authDAO.userAccount(overrideEmail)
        runApiAction(overrideEmail)(getUserInfoDetails)
      case Left(errorMsg) => responseErrorResult(errorMsg)
    }
  }

  /**
    * Update the user info details, different email will replace the older email. Only logged in user can do
    * @param id to select the user's account
    * @return User Info once the success update on the record or an error response
    */
  def updateUserInfo(id: Int): Action[AnyContent] = UserAction { implicit request =>
    log.info("Executing updateUserInfo Controller")
    // Add request validation
    AuthForms.signUpForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          badRequest(s"The update Form was not in the expected format: $formWithErrors")
        },
        userDetails => {
          authDAO.updateUserInfo(id, userDetails) match {
            case Right(user) =>
              user match {
                case Success(_) =>
                  responseOk(UserDetail(authDAO.checkValidEmail(userDetails.email).head))
                case Failure(exception) => badRequest(exception.getMessage)
              }
            case Left(exception) =>
              bcryptValidationFailed(invalidBcryptValidation(exception.getMessage))
          }
        }
      )
  }

  // sign out
  /**
    Sign out has to be implemented in the front-end side of the project
    When the user is successfully sign in, then the token has to be stored in the Session storage in the web page
    Once the user clicks the sign out button that is visible to logged in user only,
    then the session storage has to be cleared out and redirect to login page
    */
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
        case Right(value)    => responseOk(value)
        case Left(exception) => exception
      }
    } else badRequest(s"Email is in wrong format: $email")
  }
}
