package daos

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.krishna.model.QuotesQuery
import com.krishna.util.DbRunner
import helper.PlayPostgreSQLTest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend
import slick.jdbc.PostgresProfile.api._

import scala.daos.QuoteQueryDAO
import scala.io.Source

class QuoteQueryDAOSpec
    extends AnyFlatSpec
    with TestContainerForAll
    with DbRunner
    with PlayPostgreSQLTest {

  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def()

  // Start the connection
  override val dbConfig: JdbcBackend.DatabaseDef = getDb(startContainers())

  def loadSqlQueries: Seq[Int] = {
    val scripts: Seq[String] = Source
      .fromResource("testQueries/quoteTestSqlQueries.sql")
      .mkString
      .trim
      .split(";")
      .map(_.trim)
    val dbSeq = DBIO.sequence(scripts.map(s => sqlu"#$s"))
    runDbAction(dbSeq)
  }

  "PostgreSQL container" should "be started" in {
    val quoteQueryDao = new QuoteQueryDAO(dbConfig)
    loadSqlQueries

    val result: Seq[QuotesQuery] = quoteQueryDao.listAllQuotes
    result shouldBe Seq.empty
  }

}
