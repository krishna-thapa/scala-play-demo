// Auth has to be appended on the controller so that routes file can read
package controllers.auth

import java.time.Clock
import com.krishna.response.ErrorMsg.InvalidFormFormat
import com.krishna.response.ResponseResult
import com.krishna.util.FutureErrorHandler.ToFuture
import com.krishna.util.Logging
import depInject.{ SecuredController, SecuredControllerComponents }
import form.{ AuthForms, SignInForm }

import javax.inject.{ Inject, Singleton }
import play.api.mvc._
import config.DecodeHeader
import play.api.Configuration
import service.AuthService

@Singleton
class AuthController @Inject()(
    scc: SecuredControllerComponents,
    authService: AuthService
)(implicit conf: Configuration)
    extends SecuredController(scc)
    with Logging
    with ResponseResult {

  implicit val clock: Clock = Clock.systemUTC

  /**
    * Sign In the existing user using sing in form
    * @return Auth JWT token in the header if success with response body of user details
    *  Returns response error if the sign in is not success
    */
  def signIn: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    log.info("Executing signIn Controller")
    // Add request validation
    AuthForms.signInForm
      .bindFromRequest()
      .fold(
        formWithErrors => invalidForm[SignInForm](formWithErrors, InvalidFormFormat("Login")),
        signInForm => authService.signInService(signInForm)
      )
      .toFuture
  }

  /**
    * Sign up the new account in the database
    * @return Record id or an exception
    */
  def signUp: Action[AnyContent] = Action.async { implicit request =>
    log.info("Executing signUp Controller")
    // Add request validation
    AuthForms.signUpForm
      .bindFromRequest()
      .fold(
        formWithErrors => badRequest(s"The signup From was not in the expected format: $formWithErrors"),
        signUpForm => authService.singUpService(signUpForm)
      )
      .toFuture
  }

  /**
    * List all the users from the database: Only Admin can perform this action
    * @return Seq of users
    */
  def getAllUser: Action[AnyContent] = AdminAction.async { implicit request =>
    log.info("Executing getAllUser Controller")
    authService.getAllUserService.toFuture
  }

  /**
    * Alter the admin role to the selected user: Only the Admin can
    * @param email to select the user's account
    * @return Record id or an exception
    */
  def toggleAdminRole(email: String): Action[AnyContent] = AdminAction.async { implicit request =>
    log.info("Executing toggleAdminRole Controller")
    authService.toggleAdminRoleService(email).toFuture
  }

  /**
    * Remove the user account from the database: Only the Admin can
    * @param email to select the user's account
    * @return Record id or an exception
    */
  def removeUser(email: String): Action[AnyContent] = AdminAction.async { implicit request =>
    log.info("Executing removeUser Controller")
    authService.removeUserService(email).toFuture
  }

  /**
    * Get the user info from selected email: only the Admin or logged in user can
    * Logged in user can only view own record info whereas an admin can view anyone's record
    * @param email to select the user's account
    * @return Record details or an exception
    */
  def getUserInfo(email: String): Action[AnyContent] = UserAction.async { implicit request =>
    log.info("Executing getUserInfo Controller")

    val userInfo: Result = DecodeHeader(request.headers) match {
      case Right(user) =>
        authService.getUserInfoService(user, email)
      case Left(errorMsg) => responseErrorResult(errorMsg)
    }
    userInfo.toFuture
  }

  /**
    * Update the user info details, different email will replace the older email. Only logged in user can do
    * @return User Info once the success update on the record or an error response
    * TODO: Updating the email have to update jwt token, might need to refresh and update in front-end
    */
  def updateUserInfo: Action[AnyContent] = UserAction.async { implicit request =>
    log.info("Executing updateUserInfo Controller")
    DecodeHeader(request.headers) match {
      case Right(user) =>
        AuthForms.signUpForm
          .bindFromRequest()
          .fold(
            formWithErrors => badRequest(s"The update Form was not in the expected format: $formWithErrors").toFuture,
            userDetails => authService.updateUserInfoService(user.email, userDetails)
          )
      case Left(errorMsg) => responseErrorResult(errorMsg).toFuture
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
