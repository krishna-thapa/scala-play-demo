package auth.dao

import java.sql.Date

import auth.bcrypt.BcryptException
import auth.bcrypt.BcryptObject.{ encryptPassword, validatePassword }
import auth.form.{ SignInForm, SignUpForm }
import auth.model.{ UserInfo, UserList }
import auth.table.UserTable
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.Result
import response.ResponseMethod
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery
import utils.{ DbRunner, Logging }

import scala.util.{ Failure, Success }

@Singleton
class AuthDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)
    extends DbRunner
    with ResponseMethod
    with Logging {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  val userInfo: TableQuery[UserTable] = UserTable.userTableQueries

  // Common anonymous method to check if the selected email exists in the database
  val checkValidEmail: String => Seq[UserInfo] = (email: String) =>
    runDbAction(userInfo.filter(_.email === email).result)

  /**
    * Create a new user account in the table
    *
    * @param details user sign up form details
    * @return New id of the record and exception if the password hashing goes wrong
    */
  def signUpUser(details: SignUpForm): Either[Throwable, Int] = {
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
        Right(runDbAction(action))
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
    val result = runDbAction(userInfo.sortBy(_.email).result)
    result.map(UserList(_))
  }

  /**
    * Toggle the user admin role: Only Admin can perform this action
    * @param email to select the account
    * @return Either exception or success record id
    */
  def toggleAdmin(email: String): Either[Result, Int] = {
    val removeUser = (user: UserInfo) => alterAdminRole(user.id, user.isAdmin)
    findValidEmail(email)(removeUser)
  }

  /**
    * Remove the account from the database: Only the Admin can perform this action
    * @param email to select the account
    * @return Either exception or success record id
    */
  def removeUserAccount(email: String): Either[Result, Int] = {
    val removeUser = (user: UserInfo) => runDbAction(userInfo.filter(_.id === user.id).delete)
    findValidEmail(email)(removeUser)
  }

  /**
    * Checks if the email exist in the database and perform the action on the account
    * @param email to select the account
    * @param fun function to be apply in the account
    * @return Either exception or success record id
    */
  def findValidEmail(email: String)(fun: UserInfo => Int): Either[Result, Int] = {
    // check if the account exists with that email
    checkValidEmail(email).headOption match {
      case Some(account) =>
        log.info(s"Account is already in the table with id: ${account.id}")
        Right(fun(account))
      case None =>
        val errorMsg: String = s"User account is not found: $email"
        log.info(errorMsg)
        Left(notFound(errorMsg))
    }
  }

  /**
    * Alter the admin role on the selected account
    * @param id   id from user_detail_table
    * @param role boolean tag to alter the admin role
    * @return user account with updated admin role
    */
  def alterAdminRole(id: Int, role: Boolean): Int = {
    log.info(s"Changing the admin role status of: $id to ${!role}")
    runDbAction(
      userInfo
        .filter(_.id === id)
        .map(account => account.isAdmin)
        .update(!role)
    )
  }
}
