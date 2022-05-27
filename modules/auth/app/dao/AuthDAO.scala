package dao

import java.sql.Date

import bcrypt.BcryptException
import bcrypt.BcryptObject.{ encryptPassword, validatePassword }
import com.krishna.response.ErrorMsg.{ InvalidPassword, invalidBcryptValidation }
import com.krishna.response.OkResponse
import form.{ SignInForm, SignUpForm }
import javax.inject.{ Inject, Singleton }
import model.{ UserDetail, UserInfo }
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.Result
import slick.jdbc.{ JdbcBackend, JdbcProfile }
import slick.jdbc.PostgresProfile.api._

import scala.util.{ Failure, Success, Try }

@Singleton
class AuthDAO @Inject() (dbConfigProvider: DatabaseConfigProvider) extends CommonMethods {

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
          -1,
          details.firstName.capitalize,
          details.lastName.capitalize,
          details.email,
          encrypted,
          currentDate
        )
        Right(OkResponse(s"${ runDbAction(action) }"))
      case Failure(exception) => Left(BcryptException(exception.getMessage))
    }
  }

  /**
    * To check if the account already exist
    *
    * @param email user email
    * @return boolean representation
    */
  def isAccountExist(email: String): Boolean = {
    checkValidEmail(email).nonEmpty
  }

  /**
    * Check if the email and password provided are valid
    *
    * @param details user details with email and password
    * @return UserInfo if the valid user or else exception result
    */
  def isValidLogin(details: SignInForm): Either[Result, UserInfo] = {
    // Already know that account exist with that email
    val user: UserInfo = checkValidEmail(details.email).head
    // Check for the password match
    validatePassword(details.password, user.password) match {
      case Success(true)  => Right(user)
      case Success(false) => Left(unauthorized(InvalidPassword(details.email)))
      case Failure(exception) =>
        Left(bcryptValidationFailed(invalidBcryptValidation(exception.getMessage)))
    }
  }

  /**
    * List all the users from the database: Only Admin can perform this action
    * @return list of existing users
    */
  def listAllUser(): Seq[UserDetail] = {
    val result = runDbAction(userInfo.sortBy(_.email).result)
    result.map(UserDetail(_))
  }

  /**
    * Toggle the user admin role: Only Admin can perform this action
    * @param email to select the account
    * @return Either exception or success record id
    */
  def toggleAdmin(email: String): Either[Result, UserDetail] = {
    val toggleRole = (user: UserInfo) => {
      alterAdminRole(user.id, user.isAdmin) // Side-effect method
      UserDetail(checkValidEmail(email).head)
    }
    findValidEmail(email)(toggleRole)
  }

  /**
    * Remove the account from the database: Only the Admin can perform this action
    * @param email to select the account
    * @return Either exception or success record id
    */
  def removeUserAccount(email: String): Either[Result, OkResponse] = {
    val removeUser = (user: UserInfo) => {
      val result = runDbAction(userInfo.filter(_.id === user.id).delete)
      OkResponse(s"Successfully delete entry $result")
    }
    findValidEmail(email)(removeUser)
  }

  /**
    * Get the user info from the selected email: Only the Admin and logged user can
    * @param email to select the account
    * @return Either exception or success record details of the user
    */
  def userAccount(email: String): Either[Result, UserDetail] = {
    val getUser = (user: UserInfo) => UserDetail(user)
    findValidEmail(email)(getUser)
  }

  /**
    * Update the user info details: Only the logged in user can
    * @param id to select the account
    * @param details Update details searchForm
    * @return Either exception or success id of the updated record
    */
  def updateUserInfo(oldEmail: String, details: SignUpForm): Either[Throwable, Try[Int]] = {
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
        Right(runDbActionCatchError(action))
      case Failure(exception) => Left(BcryptException(exception.getMessage))
    }
  }

}
