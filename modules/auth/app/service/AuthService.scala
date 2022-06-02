package service

import com.krishna.response.ErrorMsg.{ AccountNotFound, invalidBcryptValidation }
import com.krishna.response.ResponseResult
import com.krishna.util.UtilImplicits.{ ToFuture, ValidEmail }
import com.krishna.util.Logging
import config.JwtKey
import dao.AuthDAO
import form.{ SignInForm, SignUpForm }
import model.UserDetail
import pdi.jwt.JwtSession.RichResult
import play.api.Configuration
import play.api.libs.json.OFormat
import play.api.mvc._

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class AuthService @Inject() (authDAO: AuthDAO, gridFsAttachmentService: GridFsAttachmentService)(
  implicit
  executionContext: ExecutionContext,
  config: Configuration
) extends JwtKey
    with ResponseResult
    with Logging {

  implicit val clock: Clock = Clock.systemUTC

  def signInService(signInForm: SignInForm)(implicit request: RequestHeader): Result = {
    // Need to check if the user has enter wrong password but has an account already
    if (authDAO.isAccountExist(signInForm.email)) {
      authDAO.isValidLogin(signInForm) match {
        case Right(validUser) =>
          val userDetail: UserDetail = UserDetail(validUser)
          log.info(s"Success on authentication for user: ${ userDetail.name }")
          responseOk(userDetail).addingToJwtSession(
            jwtSessionKey,
            UserDetail(validUser)
          )
        case Left(exceptionResult) => exceptionResult
      }
    } else responseErrorResult(AccountNotFound(signInForm.email))
  }

  def singUpService(signUpForm: SignUpForm): Result = {
    // need to check if the account already exist
    if (!authDAO.isAccountExist(signUpForm.email)) {
      authDAO.signUpUser(signUpForm) match {
        case Right(value) => responseOk(value)
        case Left(exception) =>
          bcryptValidationFailed(invalidBcryptValidation(exception.getMessage))
      }
    } else notAcceptable(s"${ signUpForm.email }")
  }

  def getAllUserService: Result = responseSeqResult(authDAO.listAllUser())

  def toggleAdminRoleService(email: String): Result = {
    val toggleAdmin = (email: String) => authDAO.toggleAdmin(email)
    runApiAction(email)(toggleAdmin)
  }

  def removeUserService(email: String): Result = {
    val removeAccount = (email: String) => authDAO.removeUserAccount(email)
    runApiAction(email)(removeAccount)
  }

  def getUserInfoService(user: UserDetail, email: String): Result = {
    val overrideEmail = if (user.isAdmin) email else user.email
    val getUserInfoDetails = (overrideEmail: String) => authDAO.userAccount(overrideEmail)
    runApiAction(overrideEmail)(getUserInfoDetails)
  }

  def updateUserInfoService(oldEmail: String, signUpForm: SignUpForm): Future[Result] = {
    authDAO.updateUserInfo(oldEmail, signUpForm) match {
      case Right(user) =>
        user match {
          case Success(_) =>
            gridFsAttachmentService
              .updateEmailInfo(oldEmail, signUpForm.email)
              .map(_ => responseOk(UserDetail(authDAO.checkValidEmail(signUpForm.email).head)))
          case Failure(exception) => badRequest(exception.getMessage).toFuture
        }
      case Left(exception) =>
        bcryptValidationFailed(invalidBcryptValidation(exception.getMessage)).toFuture
    }
  }

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
    if (email.validEmail) {
      fun(email) match {
        case Right(value)    => responseOk(value)
        case Left(exception) => exception
      }
    } else badRequest(s"Email is in wrong format: $email")
  }

}
