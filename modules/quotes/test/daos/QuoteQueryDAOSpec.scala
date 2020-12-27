package daos

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.krishna.model.{ Genre, QuotesQuery }
import helper.PlayPostgreSQLTest
import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{ Application, Mode }
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.JdbcBackend

import scala.daos.QuoteQueryDAO
import scala.util.{ Failure, Success }

class QuoteQueryDAOSpec
    extends PlayPostgreSQLTest
    with Matchers
    with TestContainerForAll
    with GuiceOneAppPerSuite {

  // Define a Postgres Container for test docker
  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def()

  // Start the connection
  val container: PostgreSQLContainer             = startContainers()
  override val dbConfig: JdbcBackend.DatabaseDef = getDb(container)

  // Load the test sql queries in the test Postgres docker container
  val loadQueries: Unit = loadSqlQueries match {
    case Success(_)         => log.info("Success on loading sql queries in test container")
    case Failure(exception) => log.error("Failed on loading sql queries: " + exception.getMessage)
  }

  // Initialize the QuoteQueryDao class

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "slick.dbs.default.profile"     -> "slick.jdbc.PostgresProfile$",
      "slick.dbs.default.db.driver"   -> "org.postgresql.Driver",
      "slick.dbs.default.db.url"      -> container.jdbcUrl,
      "slick.dbs.default.db.user"     -> container.username,
      "slick.dbs.default.db.password" -> container.password
    )
    .build()

//  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder()
//  lazy val injector                            = appBuilder.injector()
//  lazy val databaseApi                         = injector.instanceOf[DBApi] //here is the important line
//
//  Evolutions.applyEvolutions(databaseApi.database("test"))

  //val quoteQueryDao: QuoteQueryDAO = appBuilder.injector.instanceOf[QuoteQueryDAO]

  val quoteQueryDao: QuoteQueryDAO = Application.instanceCache[QuoteQueryDAO].apply(app)

  //val dbConfigProvider: DatabaseConfigProvider
  //lazy val appBuilder: GuiceApplicationBuilder    = new GuiceApplicationBuilder().in(Mode.Test)
  //lazy val injector: Injector                     = appBuilder.injector()
  //lazy val dbConfProvider: DatabaseConfigProvider = injector.instanceOf[DatabaseConfigProvider]

  //val quoteQueryDao: QuoteQueryDAO = app.injector.instanceOf[QuoteQueryDAO]
  //val foo: DatabaseConfigProvider  = mock[DatabaseConfigProvider]
  //val quoteQueryDao: QuoteQueryDAO = new QuoteQueryDAO(dbConfProvider)
  //val quoteQueryDao = new QuoteQueryDAO(dbConfig.createSession())

  behavior of "QuoteQueryDAO"
  it should "list down all the quotes from database" in {
    val result: Seq[QuotesQuery] = quoteQueryDao.listAllQuotes
    result.length shouldBe 3
    result.head.csvId should startWith("CSV")
  }

  it should "get a random quote from the database" in {
    val result: Seq[QuotesQuery] = quoteQueryDao.listRandomQuote(1)
    result.length shouldBe 1
    val resultWith2Records: Seq[QuotesQuery] = quoteQueryDao.listRandomQuote(2)
    resultWith2Records.length shouldBe 2
  }

  it should "get a random quote with genre provided" in {
    val result: Option[QuotesQuery] = quoteQueryDao.listGenreQuote(Genre.age)
    result.head.genre shouldBe Some(Genre.age)
  }

  it should "get None response if database is empty" in {
    val result: Option[QuotesQuery] = quoteQueryDao.listGenreQuote(Genre.alone)
    result shouldBe None
  }

  // Searched authors input with expected test outcomes
  val searchAuthorsTests: Seq[(String, Seq[String])] = Seq(
    ("Bette Davis", Seq("Bette Davis")),
    (" Davis", Seq("Bette Davis")),
    (" avis", Seq("Bette Davis")),
    ("Bette", Seq("Bette Davis", "Bette McGill")),
    ("Wrong", Seq.empty)
  )

  for (counter <- searchAuthorsTests.indices) {
    val toTest = searchAuthorsTests(counter)
    runGenericTest(
      testSearchAuthors(toTest._1, toTest._2),
      counter + 1,
      "searchAuthors()",
      "result from search authors"
    )
  }

  def testSearchAuthors(input: String, result: Seq[String]): Assertion = {
    assert(quoteQueryDao.searchAuthors(input) === result)
  }

}
