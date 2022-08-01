package dao

import com.krishna.response.ErrorMsg.AccountNotFound
import com.krishna.response.ResponseError
import com.krishna.util.{ DbRunner, Logging }
import model.UserInfo
import play.api.mvc.Result
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import table.UserTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CommonMethods extends DbRunner with ResponseError with Logging {

  val userInfo: TableQuery[UserTable] = UserTable.userTableQueries

  // Common anonymous method to check if the selected email exists in the database
  val isAccountExist: String => Future[Option[UserInfo]] = (email: String) =>
    runDbAsyncAction(userInfo.filter(_.email === email).result.headOption)

  /**
    * Checks if the email exist in the database and perform the action on the account
    *
    * @param email to select the account
    * @param fun   function to be apply in the account
    * @return Either exception or success record id
    */
  def findValidEmail[T](
    email: String
  )(fun: UserInfo => Future[T]): Future[Either[Result, T]] = {
    // check if the account exists with that email
    isAccountExist(email).flatMap {
      case Some(userInfo) =>
        log.info(s"Account is already in the table with id: ${ userInfo.id }")
        fun(userInfo).map(Right(_))
      case None =>
        notFound(AccountNotFound(email)).map(Left(_))
    }
  }

  /**
    * Alter the admin role on the selected account
    *
    * @param id   id from user_detail_table
    * @param role boolean tag to alter the admin role
    * @return user account id with updated admin role
    */
  def alterAdminRole(id: Int, role: Boolean): Future[Int] = {
    log.info(s"Changing the admin role status of: $id to ${ !role }")
    runDbAsyncAction(
      userInfo
        .filter(_.id === id)
        .map(account => account.isAdmin)
        .update(!role)
    )
  }

}
