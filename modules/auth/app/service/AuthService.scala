package service

import com.krishna.response.ErrorMsg.{ AccountNotFound, InvalidBcryptValidation }
import com.krishna.response.ResponseResult
import com.krishna.util.UtilImplicits.ValidEmail
import com.krishna.util.Logging
import config.JwtKey
import dao.AuthDAO
import form.{ SignInForm, SignUpForm }
import model.UserDetail
import pdi.jwt.JwtSession.RichResult
import play.api.Configuration
import play.api.libs.json.OFormat
import play.api.mvc.Results.Ok
import play.api.mvc._

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class AuthService @Inject() (authDAO: AuthDAO, gridFsAttachmentService: GridFsAttachmentService)(
  implicit
  executionContext: ExecutionContext,
  config: Configuration
) extends JwtKey
    with ResponseResult
    with Logging {

  implicit val clock: Clock = Clock.systemUTC

  def signInService(signInForm: SignInForm)(implicit request: RequestHeader): Future[Result] = {
    // Need to check if the user has enter wrong password but has an account already
    authDAO.isAccountExist(signInForm.email).flatMap {
      case Some(userInfo) =>
        authDAO.isValidLogin(userInfo, signInForm) match {
          case Right(validUser) =>
            val userDetail: UserDetail = UserDetail(validUser)
            log.info(s"Success on authentication for user: ${ userDetail.name }")
            responseOk(userDetail).map(
              _.addingToJwtSession(
                jwtSessionKey,
                UserDetail(validUser)
              )
            )
          case Left(exceptionResult) => exceptionResult
        }
      case _ => responseErrorResult(AccountNotFound(signInForm.email))
    }
  }

  def singUpService(signUpForm: SignUpForm): Future[Result] = {
    // need to check if the account already exist
    authDAO.isAccountExist(signUpForm.email).flatMap {
      case Some(_) => notAcceptable(s"${ signUpForm.email }")
      case _ =>
        authDAO.signUpUser(signUpForm) match {
          case Right(value) => responseOk(value)
          case Left(exception) =>
            bcryptValidationFailed(InvalidBcryptValidation(exception.getMessage))
        }
    }
  }

  def getAllUserService: Future[Result] = responseSeqResultAsync(authDAO.listAllUser())

  def toggleAdminRoleService(email: String): Future[Result] = {
    val toggleAdmin = (email: String) => authDAO.toggleAdmin(email)
    runApiAction(email)(toggleAdmin)
  }

  def removeUserService(email: String): Future[Result] = {
    val removeAccount = (email: String) => authDAO.removeUserAccount(email)
    runApiAction(email)(removeAccount)
  }

  def getUserInfoService(user: UserDetail, email: String): Future[Result] = {
    val overrideEmail = if (user.isAdmin) email else user.email
    val getUserInfoDetails = (overrideEmail: String) => authDAO.userAccount(overrideEmail)
    runApiAction(overrideEmail)(getUserInfoDetails)
  }

  def updateUserInfoService(oldEmail: String, signUpForm: SignUpForm): Future[Result] = {
    authDAO.updateUserInfo(oldEmail, signUpForm).flatMap {
      case Right(id) =>
        gridFsAttachmentService
          .updateEmailInfo(oldEmail, signUpForm.email)
          .map(_ => Ok(s"Successfully updated for the record id : $id"))
      case Left(exception) => Future.successful(exception)
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
  )(fun: String => Future[Either[Result, T]])(implicit conv: OFormat[T]): Future[Result] = {
    log.info(s"Checking the format of an email: $email")
    if (email.validEmail) {
      fun(email).flatMap {
        case Right(value)    => responseOk(value)
        case Left(exception) => Future.successful(exception)
      }
    } else badRequest(s"Email is in wrong format: $email")
  }

}
