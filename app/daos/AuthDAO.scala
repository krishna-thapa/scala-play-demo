package daos

import java.sql.Date

import auth.bcrypt.BcryptException
import auth.bcrypt.BcryptObject.{ encryptPassword, validatePassword }
import auth.form.{ SignInForm, SignUpForm }
import auth.model.UserInfo
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

  val createUser: TableQuery[UserTable] = UserTable.userTableQueries

  /**
    * Create a new user account in the table
    * @param details user sign up form details
    * @return New id of the record and exception if the password hashing goes wrong
    */
  def signUpUser(details: SignUpForm): Either[Throwable, Int] = {
    val currentDate = new Date(System.currentTimeMillis())
    encryptPassword(details.password) match {
      case Success(encrypted) =>
        val action = createUser returning createUser
          .map(_.id) += UserInfo(
          -1,
          details.firstName,
          details.lastName,
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
    * @param email user email
    * @return boolean representation
    */
  def isAccountExist(email: String): Boolean = {
    runDbAction(createUser.filter(_.email === email).result).nonEmpty
  }

  /**
    * Check if the email and password provided are valid
    * @param details user details with email and password
    * @return UserInfo if the valid user or else exception result
    */
  def isValidLogin(details: SignInForm): Either[Result, UserInfo] = {
    // Already know that account exist with that email
    val user: UserInfo = runDbAction(createUser.filter { user =>
      user.email === details.email
    }.result).head
    // Check for the password match
    validatePassword(details.password, user.password) match {
      case Success(true)      => Right(user)
      case Success(false)     => Left(unauthorized(s"${details.email}"))
      case Failure(exception) => Left(bcryptValidationFailed(exception.getMessage))
    }
  }
}
