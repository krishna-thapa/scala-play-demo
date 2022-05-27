package daos

import com.krishna.model.{ FavQuoteQuery, QuotesQuery }
import helper.testContainers.PostgresInstance
import org.scalatest.matchers.should.Matchers
import play.api.Application
import org.scalatest.TryValues._

import scala.util.Try

class FavQuoteQueryDAOSpec extends PostgresInstance with Matchers {

  // Load the quotes test sql queries in the test Postgres docker container
  loadQueries("quoteTestQueries")

  // Load the fav_quotations test sql queries in the test Postgres docker container
  loadQueries("favQuoteTestQueries")

  // Initialize the FavQuoteQueryDAO class
  val favQuoteQueryDAO: FavQuoteQueryDAO = Application.instanceCache[FavQuoteQueryDAO].apply(app)

  behavior of "FavQuoteQueryDAO"

  it should "return empty if the are no fav quotes for a user id" in {
    val resultFirstUser: Seq[QuotesQuery] = favQuoteQueryDAO.listFavQuotes(1)
    resultFirstUser.length shouldBe 0
  }

  it should "create a new fav record if the record with same user id is not present" in {
    val result: Try[FavQuoteQuery] = favQuoteQueryDAO.modifyFavQuote(1, "CSV101")
    result.success.value.csvId shouldBe "CSV101"
    result.success.value.favTag shouldBe true
  }

  it should "return list of quotes for a user id" in {
    favQuoteQueryDAO.modifyFavQuote(1, "CSV102")
    val allFavQuotes: Seq[QuotesQuery] = favQuoteQueryDAO.listFavQuotes(1)
    allFavQuotes.length shouldBe 2
    allFavQuotes.map(_.csvId) should contain theSameElementsAs Seq("CSV101", "CSV102")
  }

  it should "modify the favTag boolean if the existing record is requested" in {
    val result: Try[FavQuoteQuery] = favQuoteQueryDAO.modifyFavQuote(1, "CSV102")
    result.success.value.csvId shouldBe "CSV102"
    result.success.value.favTag shouldBe false
  }

  it should "return the fav quotes that has favTag as true only" in {
    val allFavQuotes: Seq[QuotesQuery] = favQuoteQueryDAO.listFavQuotes(1)
    allFavQuotes.length shouldBe 1
  }

}
