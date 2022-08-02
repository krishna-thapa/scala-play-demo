package daos

import com.krishna.model.{ Genre, QuotesQuery }
import helper.testContainers.PostgresInstance
import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should.Matchers
import play.api.Application

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, Future }

class QuoteQueryDAOSpec extends PostgresInstance with Matchers {

  // Load the test sql queries in the test Postgres docker container
  loadQueries("quoteTestQueries")

  // Initialize the QuoteQueryDao class
  val quoteQueryDao: QuoteQueryDAO = Application.instanceCache[QuoteQueryDAO].apply(app)

  behavior of "QuoteQueryDAO"

  it should "list down all the quotes from database" in {
    val response: Future[Seq[QuotesQuery]] = quoteQueryDao.listAllQuotes(100, 0)
    response.map { result =>
      result.length shouldBe 5
      result.head.csvId should startWith("CSV")
    }

  }

  it should "get a random quote from the database" in {
    val response: Future[Seq[QuotesQuery]] = quoteQueryDao.listRandomQuote(1)
    response.map { result =>
      result.length shouldBe 1
    }
    val responseWith2Records: Future[Seq[QuotesQuery]] = quoteQueryDao.listRandomQuote(2)
    responseWith2Records.map { result =>
      result.length shouldBe 2
    }
  }

  it should "get a random quote with genre provided" in {
    val response: Future[Option[QuotesQuery]] = quoteQueryDao.listGenreQuote(Genre.age)
    response.map { result =>
      result.head.genre shouldBe Some(Genre.age)
    }
  }

  it should "get None response if database is empty" in {
    val response: Future[Option[QuotesQuery]] = quoteQueryDao.listGenreQuote(Genre.alone)
    response.map { result =>
      result shouldBe None
    }
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
    val futureResult = Await.result(quoteQueryDao.searchAuthors(input), 10.seconds)
    assert(futureResult === result)
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
    }
    else
      it should testDesc in {
        assertion
      }
  }

}
