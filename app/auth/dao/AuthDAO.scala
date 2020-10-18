package auth.dao

import java.sql.Date

import auth.bcrypt.BcryptException
import auth.bcrypt.BcryptObject.{ encryptPassword, validatePassword }
import auth.form.{ SignInForm, SignUpForm }
import auth.model.{ UserInfo, UserList }
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.Result
import response.OkResponse
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.util.{ Failure, Success, Try }

@Singleton
class AuthDAO @Inject()(dbConfigProvider: DatabaseConfigProvider) extends CommonMethods {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  /**
    * Create a new user account in the table
    *
    * @param details user sign up form details
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
        Right(OkResponse(s"${runDbAction(action)}"))
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
      case Success(true)      => Right(user)
      case Success(false)     => Left(unauthorized(s"${details.email}"))
      case Failure(exception) => Left(bcryptValidationFailed(exception.getMessage))
    }
  }

  /**
    * List all the users from the database: Only Admin can perform this action
    * @return list of existing users
    */
  def listAllUser(): Seq[UserList] = {
    println("helloe" + encryptPassword("admin"))
    val result = runDbAction(userInfo.sortBy(_.email).result)
    result.map(UserList(_))
  }

  /**
    * Toggle the user admin role: Only Admin can perform this action
    * @param email to select the account
    * @return Either exception or success record id
    */
  def toggleAdmin(email: String): Either[Result, UserList] = {
    val toggleRole = (user: UserInfo) => {
      alterAdminRole(user.id, user.isAdmin) // Side-effect method
      UserList(checkValidEmail(email).head)
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
  def userAccount(email: String): Either[Result, UserList] = {
    val getUser = (user: UserInfo) => UserList(user)
    findValidEmail(email)(getUser)
  }

  /**
    * Update the user info details: Only the logged in user can
    * @param id to select the account
    * @param details Update details form
    * @return Either exception or success id of the updated record
    */
  def updateUserInfo(id: Int, details: SignUpForm): Either[Throwable, Try[Int]] = {
    encryptPassword(details.password) match {
      case Success(encrypted) =>
        val action = userInfo
          .filter(_.id === id)
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
