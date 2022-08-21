// Auth has to be appended on the controller so that routes file can read
package controllers.auth

import com.krishna.response.ErrorMsg.InvalidFormFormat
import com.krishna.response.ResponseResult
import com.krishna.util.Logging
import config.DecodeHeader
import depInject.{ SecuredController, SecuredControllerComponents }
import form.{ AuthForms, SignInForm, SignUpForm }
import play.api.Configuration
import play.api.libs.Files
import play.api.mvc._
import service.AuthService

import java.time.Clock
import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future

@Singleton
class AuthController @Inject() (
  scc: SecuredControllerComponents,
  authService: AuthService
)(implicit conf: Configuration)
    extends SecuredController(scc)
    with Logging
    with ResponseResult {

  implicit val clock: Clock = Clock.systemUTC

  /**
    * Sign In the existing user
    * @return Auth JWT token in the header if success with response body of user details
    *  Returns response error if the sign in is not success
    */
  def signIn: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    log.info("Executing signIn method in AuthController.")
    AuthForms
      .signInForm
      .bindFromRequest()
      .fold(
        formWithErrors => invalidForm[SignInForm](formWithErrors, InvalidFormFormat("SignInForm")),
        signInForm => authService.signInService(signInForm)
      )
  }

  /**
    * Sign up the new account in the database
    * @return Record id or an exception
    */
  def signUp: Action[AnyContent] = Action.async { implicit request =>
    log.info("Executing signUp method in AuthController.")
    AuthForms
      .signUpForm
      .bindFromRequest()
      .fold(
        formWithErrors => invalidForm[SignUpForm](formWithErrors, InvalidFormFormat("SignUpForm")),
        signUpForm => authService.singUpService(signUpForm)
      )
  }

  /**
    * List all the users from the database: Only Admin can perform this action
    * @return Seq of users
    */
  def getAllUser: Action[AnyContent] = AdminAction.async { implicit request =>
    log.info("Executing getAllUser method AuthController.")
    authService.getAllUserService
  }

  /**
    * Alter the admin role of the selected user: Only the Admin can
    * @param email to select the user's account
    * @return Record id or an exception
    */
  def toggleAdminRole(email: String): Action[AnyContent] = AdminAction.async { implicit request =>
    log.info("Executing toggleAdminRole method in AuthController.")
    authService.toggleAdminRoleService(email)
  }

  /**
    * Remove the user account from the database: Only the Admin can
    * @param email to select the user's account
    * @return Record id or an exception
    */
  def removeUser(email: String): Action[AnyContent] = AdminAction.async { implicit request =>
    log.info("Executing removeUser Controller")
    authService.removeUserService(email)
  }

  /**
    * Get the user info from selected email: only the Admin or logged in user can
    * Logged in user can only view own record info whereas an admin can view anyone's record
    * @param email to select the user's account
    * @return Record details or an exception
    */
  def getUserInfo(email: String): Action[AnyContent] = UserAction.async { implicit request =>
    log.info("Executing getUserInfo method in AuthController.")
    DecodeHeader(request.headers) match {
      case Right(user) =>
        authService.getUserInfoService(user, email)
      case Left(errorMsg) => responseErrorResult(errorMsg)
    }
  }

  /**
    * Update the user info details, different email will replace the older email. Only logged in user can do
    * @return User Info once the success update on the record or an error response
    * TODO: Updating the email have to update jwt token, might need to refresh and update in front-end
    */
  def updateUserInfo: Action[AnyContent] = UserAction.async { implicit request =>
    log.info("Executing updateUserInfo method in AuthController.")
    DecodeHeader(request.headers) match {
      case Right(user) =>
        AuthForms
          .signUpForm
          .bindFromRequest()
          .fold(
            formWithErrors =>
              invalidForm[SignUpForm](formWithErrors, InvalidFormFormat("UpdateSignUpForm")),
            userDetails => authService.updateUserInfoService(user.email, userDetails)
          )
      case Left(errorMsg) => responseErrorResult(errorMsg)
    }
  }

  /**
   * Save the user's profile picture in the Postgres database as Array of bytes
   * @return Success on inserting a new picture or updating picture or an error response
   */
  def insertProfilePic: Action[MultipartFormData[Files.TemporaryFile]] =
    UserAction.async(parse.multipartFormData) { request =>
      DecodeHeader(request.headers) match {
        case Right(user) =>
          log.info(
            s"Executing insertProfilePic for the user: ${ user.email } method in AuthController."
          )
          request
            .body
            .files
            .headOption
            .map { picture =>
              log.info(
                s"Trying to upload profile pic: ${ picture.filename }, of type ${ picture.contentType } for user: ${ user.email }"
              )
              authService.uploadUserProfilePic(user, picture)
            }
            .getOrElse {
              Future.successful(NoContent)
            }
        case Left(errorMsg) => responseErrorResult(errorMsg)
      }
    }

  /**
   * Retrieve the user's profile picture, the picture details will be in the HTTP response header
   * @return Picture of the users profile
   */
  def getAttachedPicture: Action[AnyContent] = UserAction.async { request =>
    DecodeHeader(request.headers) match {
      case Right(user) =>
        log.info(
          s"Executing getAttachedPicture for the request user email: ${ user.email } in AuthController."
        )
        authService.getUserProfilePic(user)
      case Left(errorMsg) => responseErrorResult(errorMsg)
    }
  }

  /**
   * Delete the user's profile picture
   * @return Success or error while deleting the picture
   */
  def removeProfilePicture: Action[AnyContent] = UserAction.async { request =>
    DecodeHeader(request.headers) match {
      case Right(user) =>
        log.info(
          s"Executing removeProfilePicture for the request user email: ${ user.email } in AuthController."
        )
        authService.deleteUserProfilePic(user)
      case Left(errorMsg) => responseErrorResult(errorMsg)
    }
  }

  // sign out
  /**
    Sign out has to be implemented in the front-end side of the project
    When the user is successfully sign in, then the token has to be stored in the Session storage in the web page
    Once the user clicks the sign out button that is visible to logged in user only,
    then the session storage has to be cleared out and redirect to login page
  */
}
