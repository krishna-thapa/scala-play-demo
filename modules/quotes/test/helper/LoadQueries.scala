package helper

import com.krishna.util.DbRunner
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.io.Source
import scala.util.{ Failure, Success, Try }

trait LoadQueries extends DbRunner {

  def loadQueries(sqlTestFileName: String): Unit = {
    runSqlQueries(sqlTestFileName) match {
      case Success(_)         => log.info("Success on loading sql queries in test container")
      case Failure(exception) => log.error("Failed on loading sql queries: " + exception.getMessage)
    }
  }

  // Load the test sql queries in the test Postgres docker container
  private def runSqlQueries(sqlTestFileName: String): Try[Seq[Int]] = {
    val scripts: Seq[String] = Source
      .fromResource(s"testQueries/$sqlTestFileName.sql")
      .mkString
      .trim
      .split(";")
      .map(_.trim)
    val dbSeq = DBIO.sequence(scripts.map(s => sqlu"#$s"))
    runDbActionCatchError(dbSeq)
  }
}
