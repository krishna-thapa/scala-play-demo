package dao

import akka.stream.scaladsl.Source
import akka.util.ByteString
import bcrypt.BcryptException
import bcrypt.BcryptObject.{ encryptPassword, validatePassword }
import com.krishna.model.auth.ProfilePictureInfo
import com.krishna.response.ErrorMsg.{
  InvalidBcryptValidation,
  InvalidPassword,
  ProfilePicErrorMessage
}
import com.krishna.response.OkResponse
import form.{ SignInForm, SignUpForm }
import model.{ UserDetail, UserInfo }
import play.api.db.slick.DatabaseConfigProvider
import play.api.http.HttpEntity
import play.api.http.Status.OK
import play.api.libs.{ Files => PlayFile }
import play.api.mvc.{ MultipartFormData, ResponseHeader, Result }
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{ JdbcBackend, JdbcProfile }

import java.nio.file.{ Files, Paths }
import java.sql.Date
import java.util.UUID
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

@Singleton
class AuthDAO @Inject() (implicit
  executionContext: ExecutionContext,
  dbConfigProvider: DatabaseConfigProvider
) extends CommonMethods {

  override val dbConfig: JdbcBackend#DatabaseDef = dbConfigProvider.get[JdbcProfile].db

  /**
    * Create a new user account in the table
    *
    * @param details user sign up searchForm details
    * @return New id of the record and exception if the password hashing goes wrong
    */
  def signUpUser(details: SignUpForm): Either[Throwable, OkResponse] = {
    val currentDate = new Date(System.currentTimeMillis())
    encryptPassword(details.password) match {
      case Success(encrypted) =>
        val action = userInfo returning userInfo
          .map(_.id) += UserInfo(
          UUID.randomUUID(),
          details.firstName.capitalize,
          details.lastName.capitalize,
          details.email,
          encrypted,
          currentDate,
          profilePictureInfo = None,
          profilePicture = Array[Byte]()
        )
        Right(OkResponse(s"${ runDbAction(action) }"))
      case Failure(exception) => Left(BcryptException(exception.getMessage))
    }
  }

  /**
    * Check if the email and password provided are valid
    *
    * @param details user details with email and password
    * @return UserInfo if the valid user or else exception result
    */
  def isValidLogin(userInfo: UserInfo, details: SignInForm): Either[Future[Result], UserInfo] = {
    // Check for the password match
    validatePassword(details.password, userInfo.password) match {
      case Success(true)  => Right(userInfo)
      case Success(false) => Left(unauthorized(InvalidPassword(details.email)))
      case Failure(exception) =>
        Left(bcryptValidationFailed(InvalidBcryptValidation(exception.getMessage)))
    }
  }

  /**
    * List all the users from the database: Only Admin can perform this action
    * @return list of existing users
    */
  def listAllUser(): Future[Seq[UserDetail]] = {
    val result = runDbAsyncAction(userInfo.sortBy(_.email).result)
    result.map(_.map(UserDetail(_)))
  }

  /**
    * Toggle the user admin role: Only Admin can perform this action
    * @param email to select the account
    * @return Either exception or success record id
    */
  def toggleAdmin(email: String): Future[Either[Result, OkResponse]] = {
    val toggleRole = (user: UserInfo) => {
      alterAdminRole(user.userId, user.isAdmin).map { _ =>
        OkResponse(s"Successfully toggled the admin role for entry $email")
      }
    }
    findValidEmail(email)(toggleRole)
  }

  /**
    * Remove the account from the database: Only the Admin can perform this action
    * @param email to select the account
    * @return Either exception or success record id
    */
  def removeUserAccount(email: String): Future[Either[Result, OkResponse]] = {
    val removeUser = (user: UserInfo) => {
      runDbAsyncAction(userInfo.filter(_.id === user.userId).delete)
        .map { result =>
          OkResponse(s"Successfully delete entry $result")
        }
    }
    findValidEmail(email)(removeUser)
  }

  /**
    * Get the user info from the selected email: Only the Admin and logged user can
    * @param email to select the account
    * @return Either exception or success record details of the user
    */
  def userAccount(email: String): Future[Either[Result, UserDetail]] = {
    val getUser = (user: UserInfo) => Future.successful(UserDetail(user))
    findValidEmail(email)(getUser)
  }

  /**
    * Update the user info details: Only the logged in user can perform this
    * @param oldEmail to select the account
    * @param details Update details searchForm
    * @return Either exception or success id of the updated record
    */
  def updateUserInfo(oldEmail: String, details: SignUpForm): Future[Either[Result, Int]] = {
    encryptPassword(details.password) match {
      case Success(encrypted) =>
        val action = userInfo
          .filter(_.email === oldEmail)
          .map(user => (user.firstName, user.lastName, user.email, user.password))
          .update(
            details.firstName,
            details.lastName,
            details.email,
            encrypted
          )
        runDbAsyncAction(action).map(Right(_))
      case Failure(exception) =>
        bcryptValidationFailed(InvalidBcryptValidation(exception.getMessage)).map(Left(_))
    }
  }

  /**
   * Insert a new profile picture or update the existing one along with picture details in JSON format
   * @param user User can only perform this action
   * @param picture In Java File type format
   * @return Success or error while performing action in Postgres DB
   */
  def insertOrUpdatePic(
    user: UserDetail,
    picture: MultipartFormData.FilePart[PlayFile.TemporaryFile]
  ): Future[Either[Result, OkResponse]] = {
    val pictureAsByteArray = Files.readAllBytes(Paths.get(picture.ref.getPath))
    val pictureInfo = ProfilePictureInfo(
      filename = picture.filename,
      contentType = picture.contentType,
      fileSize = picture.fileSize,
      dispositionType = picture.dispositionType
    )
    val saveUserPicture = (userRow: UserInfo) =>
      runDbAsyncAction(
        userInfo.insertOrUpdate(
          userRow.copy(profilePictureInfo = Some(pictureInfo), profilePicture = pictureAsByteArray)
        )
      )
        .map { result =>
          log.info(
            s"Successfully inserted or updated an entry $result for the user: ${ user.email }"
          )
          OkResponse(s"Successfully inserted or updated an entry $result")
        }

    findUserById(user.userId)(saveUserPicture)
  }

  /**
   * Get the user's profile picture from the Postgres Database that is stored as Array of bytes
   * Convert the Byte array to Source of Akka ByteString and send as Result for API response
   * @param user User details
   * @return Result of Stream containing the user's profile picture
   */
  def getProfilePicture(user: UserDetail): Future[Either[Result, Result]] = {
    val getUserPicture = (user: UserInfo) => {
      if (user.profilePictureInfo.isDefined) {
        val getPictureInfo = user.profilePictureInfo.get
        val source: Source[ByteString, _] = Source.single(ByteString.apply(user.profilePicture))

        log.info(s"Profile picture found for the user: ${ user.email }")
        val pictureAsArrayByte = Result(
          header = ResponseHeader(OK, Map("Content-file-name" -> s"${ getPictureInfo.filename }")),
          body =
            HttpEntity.Streamed(source, Option(getPictureInfo.fileSize), getPictureInfo.contentType)
        )
        Future(pictureAsArrayByte)
      } else {
        log.info(s"Profile picture couldn't found for the user: ${ user.email }")
        notFound(
          ProfilePicErrorMessage(s"Profile picture not found for user: ${ user.email }")
        )
      }

    }

    findUserById(user.userId)(getUserPicture)
  }

  /**
   * Delete the profile picture for the user
   * @param user User details
   * @return Success while deleting the profile pciture or error
   */
  def deleteProfilePicture(user: UserDetail): Future[Either[Result, OkResponse]] = {
    val saveUserPicture = (userRow: UserInfo) =>
      runDbAsyncAction(
        userInfo.insertOrUpdate(
          userRow.copy(profilePictureInfo = None, profilePicture = Array[Byte]())
        )
      ).map { result =>
        log.info(
          s"Successfully delete profile picture for user: ${ user.email }"
        )
        OkResponse(s"Successfully deleted profile picture for user: ${ user.email }")
      }
    findUserById(user.userId)(saveUserPicture)
  }

}
