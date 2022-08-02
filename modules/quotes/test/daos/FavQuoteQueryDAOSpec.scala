package daos

import com.krishna.model.{ FavQuoteQuery, QuotesQuery }
import helper.testContainers.PostgresInstance
import org.scalatest.matchers.should.Matchers
import play.api.Application

import scala.concurrent.Future

class FavQuoteQueryDAOSpec extends PostgresInstance with Matchers {

  // Load the quotes test sql queries in the test Postgres docker container
  loadQueries("quoteTestQueries")

  // Load the fav_quotations test sql queries in the test Postgres docker container
  loadQueries("favQuoteTestQueries")

  // Initialize the FavQuoteQueryDAO class
  val favQuoteQueryDAO: FavQuoteQueryDAO = Application.instanceCache[FavQuoteQueryDAO].apply(app)

  behavior of "FavQuoteQueryDAO"

  it should "return empty if the are no fav quotes for a user id" in {
    val resultFirstUser: Future[Seq[QuotesQuery]] = favQuoteQueryDAO.listFavQuotes(1)
    resultFirstUser.map { result =>
      result.length shouldBe 0
    }
  }

  it should "create a new fav record if the record with same user id is not present" in {
    val response: Future[FavQuoteQuery] = favQuoteQueryDAO.modifyFavQuote(1, "CSV101")
    response.map { result =>
      result.csvId shouldBe "CSV101"
      result.favTag shouldBe true
    }
  }

  it should "return list of quotes for a user id" in {
    favQuoteQueryDAO.modifyFavQuote(1, "CSV102")
    val response: Future[Seq[QuotesQuery]] = favQuoteQueryDAO.listFavQuotes(1)
    response.map { favQuotes =>
      favQuotes.length shouldBe 2
      favQuotes.map(_.csvId) should contain theSameElementsAs Seq("CSV101", "CSV102")
    }
  }

  it should "modify the favTag boolean if the existing record is requested" in {
    val response: Future[FavQuoteQuery] = favQuoteQueryDAO.modifyFavQuote(1, "CSV102")
    response.map { result =>
      result.csvId shouldBe "CSV102"
      result.favTag shouldBe false
    }
  }

  it should "return the fav quotes that has favTag as true only" in {
    val response: Future[Seq[QuotesQuery]] = favQuoteQueryDAO.listFavQuotes(1)
    response.map { favQuotes =>
      favQuotes.length shouldBe 1
    }
  }

  it should "return the cached fav quotes from the database" in {
    val response: Future[Seq[QuotesQuery]] = favQuoteQueryDAO.listCachedFavQuotes(1)
    response.map { favQuotes =>
      favQuotes.length shouldBe 1
    }
  }

}
