package dao

import helper.testContainers.PostgresInstance
import models.CustomQuotesQuery
import org.scalatest.matchers.should.Matchers
import play.api.Application

class CustomQuoteQueryDAOSpec extends PostgresInstance with Matchers {

  // Load the custom_quotations test sql queries in the test Postgres docker container
  loadQueries("customQuoteTestQueries")

  // Initialize the customQuoteQueryDAO class
  val customQuoteQueryDAO: CustomQuoteQueryDAO =
    Application.instanceCache[CustomQuoteQueryDAO].apply(app)

  behavior of "customQuoteQueryDAO"
  it should "return empty if the are no custom quotes for a user id" in {
    val resultFirstUser: Seq[CustomQuotesQuery] = customQuoteQueryDAO.listAllQuotes(1)
    resultFirstUser.length shouldBe 0
  }
}
