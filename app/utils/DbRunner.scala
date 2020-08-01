package utils

import slick.basic.DatabaseConfig
import slick.dbio.Effect.All
import slick.dbio.{ DBIOAction, NoStream }
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import utils.Implicits._

import scala.util.{ Failure, Success, Try }

trait DbRunner extends Logging {

  val dbConfig: DatabaseConfig[JdbcProfile]

  /* Run and get the result from the future*/
  def runDbAction[T](action: DBIOAction[T, NoStream, All], timeout: Option[Int] = None): T = {
    dbConfig.db.run(action.transactionally).andGetResult(timeout)
  }

  /* Run and get the result from the future or catch any error*/
  def runDbActionCatchError[T](
      action: DBIOAction[T, NoStream, All],
      timeout: Option[Int] = None
  ): Try[T] = {
    try {
      Success(runDbAction(action, timeout))
    } catch {
      case ex: Exception =>
        log.warn(" Db Action failed", ex)
        Failure(ex)
    }
  }
}
