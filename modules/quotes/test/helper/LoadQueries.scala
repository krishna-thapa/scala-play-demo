package helper

import com.krishna.util.DbRunner
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.io.Source

trait LoadQueries extends DbRunner {

  def loadQueries(sqlTestFileName: String): Unit = {
    try {
      runSqlQueries(sqlTestFileName)
      log.info("Success on loading sql queries in test container")
    } catch {
      case ex: Exception => log.error("Failed on loading sql queries: " + ex.getMessage)
    }
  }

  // Load the test sql queries in the test Postgres docker container
  private def runSqlQueries(sqlTestFileName: String): Seq[Int] = {
    val scripts: Seq[String] = Source
      .fromResource(s"testQueries/$sqlTestFileName.sql")
      .mkString
      .trim
      .split(";")
      .map(_.trim)
    val dbSeq = DBIO.sequence(scripts.map(s => sqlu"#$s"))
    Await.result(runDbAsyncAction(dbSeq), 10.seconds)
  }

}
