package dao

import com.krishna.response.ResponseError
import com.krishna.util.{ DbRunner, Logging }
import model.UserInfo
import play.api.mvc.Result
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import table.UserTable

trait CommonMethods extends DbRunner with ResponseError with Logging {

  val userInfo: TableQuery[UserTable] = UserTable.userTableQueries

  // Common anonymous method to check if the selected email exists in the database
  val checkValidEmail: String => Seq[UserInfo] = (email: String) =>
    runDbAction(userInfo.filter(_.email === email).result)

  /**
    * Checks if the email exist in the database and perform the action on the account
    *
    * @param email to select the account
    * @param fun   function to be apply in the account
    * @return Either exception or success record id
    */
  def findValidEmail[T](email: String)(fun: UserInfo => T): Either[Result, T] = {
    // check if the account exists with that email
    checkValidEmail(email).headOption match {
      case Some(userInfo) =>
        log.info(s"Account is already in the table with id: ${userInfo.id}")
        Right(fun(userInfo))
      case None =>
        val errorMsg: String = s"User account is not found: $email"
        log.info(errorMsg)
        Left(notFound(errorMsg))
    }
  }

  /**
    * Alter the admin role on the selected account
    *
    * @param id   id from user_detail_table
    * @param role boolean tag to alter the admin role
    * @return user account id with updated admin role
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
