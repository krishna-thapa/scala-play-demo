package helper

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.krishna.util.DbRunner
import org.scalatest.compatible.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._

import scala.io.Source
import scala.util.Try

trait PlayPostgreSQLTest extends AnyFlatSpec with DbRunner {

  // Initialize the Database connection using PostgreSQLContainer
  def getDb(container: PostgreSQLContainer): JdbcBackend.DatabaseDef = {
    Database.forURL(
      url = container.jdbcUrl,
      user = container.username,
      password = container.password,
      driver = "org.postgresql.Driver"
    )
  }

  // Load the test sql queries in the test Postgres docker container
  def loadSqlQueries: Try[Seq[Int]] = {
    val scripts: Seq[String] = Source
      .fromResource("evolutions.mydb/1.sql")
      .mkString
      .trim
      .split(";")
      .map(_.trim)
    val dbSeq = DBIO.sequence(scripts.map(s => sqlu"#$s"))
    runDbActionCatchError(dbSeq)
  }

  // For the generic test cases
  def runGenericTest(
      assertion: => Assertion,
      counter: Int,
      subject: "searchAuthors()",
      desc: "result from search authors"
  ): Unit = {
    val testDesc: String = s"$desc (test no: $counter)"
    if (counter == 0) subject should testDesc in {
      assertion
    } else
      it should testDesc in {
        assertion
      }
  }
}
