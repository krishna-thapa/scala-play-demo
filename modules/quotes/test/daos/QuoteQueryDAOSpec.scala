package daos

import com.krishna.model.{ Genre, QuotesQuery }
import helper.testContainers.PostgresInstance
import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should.Matchers
import play.api.Application

class QuoteQueryDAOSpec extends PostgresInstance with Matchers {

  // Load the test sql queries in the test Postgres docker container
  loadQueries("quoteTestSqlQueries")

  // Initialize the QuoteQueryDao class
  val quoteQueryDao: QuoteQueryDAO = Application.instanceCache[QuoteQueryDAO].apply(app)

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
