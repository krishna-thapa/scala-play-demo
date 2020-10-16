package daos

import java.sql.Date

import auth.bcrypt.BcryptException
import auth.bcrypt.BcryptObject.encryptPassword
import auth.form.{ SignInForm, SignUpForm }
import auth.model.UserInfo
import auth.table.UserTable
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery
import utils.{ DbRunner, Logging }

import scala.util.{ Failure, Success }

@Singleton
class AuthDAO @Inject()(dbConfigProvider: DatabaseConfigProvider) extends DbRunner with Logging {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  val createUser: TableQuery[UserTable] = UserTable.userTableQueries

  /**
    * Create a new user account in the table
    * @param details user sign up form details
    * @return New id of the record
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
    * @return UserInfo oif the valid user
    */
  def isValidLogin(details: SignInForm): Option[UserInfo] = {
    runDbAction(createUser.filter { user =>
      user.email === details.email && user.password === details.password
    }.result)
  }.headOption
}
