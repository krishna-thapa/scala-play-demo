package dao

import com.krishna.model.Genre
import forms.CustomQuoteForm
import helper.testContainers.PostgresInstance
import model.UserDetail
import models.CustomQuotesQuery
import org.scalatest.matchers.should.Matchers
import play.api.Application

import scala.util.Try

class CustomQuoteQueryDAOSpec extends PostgresInstance with Matchers {

  // Load the custom_quotations test sql queries in the test Postgres docker container
  loadQueries("customQuoteTestQueries")

  // Initialize the customQuoteQueryDAO class
  val customQuoteQueryDAO: CustomQuoteQueryDAO = Application.instanceCache[CustomQuoteQueryDAO].apply(app)

  val customQuoteForm: CustomQuoteForm = CustomQuoteForm(
    quote = "Demo quote1",
    author = Some("Dummy author"),
    genre = Some(Genre.age),
    ownQuote = true
  )
  val userInfo: UserDetail = UserDetail(
    id = 1,
    name = "User-1",
    email = "user@com",
    joinedDate = new java.sql.Date(System.currentTimeMillis()),
    isAdmin = false
  )

  behavior of "customQuoteQueryDAO"
  "listAllQuotes" should "return empty if the are no custom quotes for a user id" in {
    val resultFirstUser: Seq[CustomQuotesQuery] = customQuoteQueryDAO.listAllQuotes(1)
    resultFirstUser.length shouldBe 0
  }

  "createQuote" should "add customer quote query in custom_quotations table with user id" in {
    customQuoteQueryDAO.createQuote(customQuoteForm, userInfo)

    val resultFirstUser: Seq[CustomQuotesQuery] = customQuoteQueryDAO.listAllQuotes(1)
    resultFirstUser.length shouldBe 1
    resultFirstUser.head.quote shouldBe "Demo quote1"
    resultFirstUser.head.author.get shouldBe "User-1"
  }

  it should "add customer quote with author name if the ownQuote parameter is false" in {
    customQuoteQueryDAO.createQuote(
      customQuoteForm.copy(quote = "Demo quote2", ownQuote = false),
      userInfo.copy(id = 2, name = "User-2")
    )

    val resultFirstUser: Seq[CustomQuotesQuery] = customQuoteQueryDAO.listAllQuotes(2)
    resultFirstUser.length shouldBe 1
    resultFirstUser.head.author.get shouldBe "Dummy author"
  }

  "listSelectedQuote" should "get the selected quote for logged in user only" in {
    val result: Option[CustomQuotesQuery] = customQuoteQueryDAO.listSelectedQuote(2, 2)
    result.isDefined shouldBe true
    result.get.quote shouldBe "Demo quote2"
  }

  it should "return empty if the user has not that record in the database" in {
    val result: Option[CustomQuotesQuery] = customQuoteQueryDAO.listSelectedQuote(3, 2)
    result.isDefined shouldBe false
  }

  "listRandomQuote" should "return any random record for the logged in user only" in {
    val result: Seq[CustomQuotesQuery] = customQuoteQueryDAO.listRandomQuote(1, 1)
    result.length shouldBe 1
    result.head.userId shouldBe 1
  }

  "deleteQuote" should "delete the selected quote from the table" in {
    customQuoteQueryDAO.createQuote(
      customQuoteForm.copy(quote = "Demo quote3"),
      userInfo.copy(id = 3, name = "User-3")
    )
    val beforeDelete: Seq[CustomQuotesQuery] = customQuoteQueryDAO.listAllQuotes(3)
    beforeDelete.length shouldBe 1

    customQuoteQueryDAO.deleteQuote(3, 3)

    val afterDelete: Seq[CustomQuotesQuery] = customQuoteQueryDAO.listAllQuotes(3)
    afterDelete.length shouldBe 0
  }

  "updateQuote" should "update the existing record for logged iun user" in {
    val updateRecord: Try[Int] =
      customQuoteQueryDAO.updateQuote(
        2,
        2,
        customQuoteForm.copy(quote = "Updated quote", author = Some("Updated author"))
      )
    updateRecord.isSuccess shouldBe true
    updateRecord.get shouldBe 1

    val result: Option[CustomQuotesQuery] = customQuoteQueryDAO.listSelectedQuote(2, 2)
    result.isDefined shouldBe true
    result.get.quote shouldBe "Updated quote"
    result.get.author.get shouldBe "Updated author"
  }
}
