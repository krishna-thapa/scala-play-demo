package daos

import java.sql.Date

import auth.form.SignUpForm
import auth.model.SignUp
import auth.table.UserTable
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery
import utils.{ DbRunner, Logging }

@Singleton
class AuthDAO @Inject()(dbConfigProvider: DatabaseConfigProvider) extends DbRunner with Logging {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  val createUser: TableQuery[UserTable] = UserTable.userTableQueries

  def signUpUser(details: SignUpForm): Int = {
    val currentDate = new Date(System.currentTimeMillis())
    val action = createUser returning createUser
      .map(_.id) += SignUp(
      -1,
      details.firstName,
      details.lastName,
      details.email,
      details.password,
      currentDate
    )
    runDbAction(action)
  }
}
