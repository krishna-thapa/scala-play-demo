package dao

import com.krishna.model.Genre
import forms.RequestForm.CustomQuoteForm
import helper.testContainers.PostgresInstance
import model.UserDetail
import models.CustomQuotesQuery
import org.scalatest.matchers.should.Matchers
import play.api.Application

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, Future }

class CustomQuoteQueryDAOSpec extends PostgresInstance with Matchers {

  // Load the custom_quotations test sql queries in the test Postgres docker container
  loadQueries("customQuoteTestQueries")

  // Initialize the customQuoteQueryDAO class
  val customQuoteQueryDAO: CustomQuoteQueryDAO =
    Application.instanceCache[CustomQuoteQueryDAO].apply(app)

  val customQuoteForm: CustomQuoteForm = CustomQuoteForm(
    quote = "Demo quote1",
    author = Some("Dummy author"),
    genre = Some(Genre.age),
    ownQuote = true
  )

  val userUuid1: UUID = UUID.randomUUID()
  val userUuid2: UUID = UUID.randomUUID()
  val userUuid3: UUID = UUID.randomUUID()

  val userInfo: UserDetail = UserDetail(
    userId = userUuid1,
    name = "User-1",
    email = "user@com",
    joinedDate = new java.sql.Date(System.currentTimeMillis()),
    isAdmin = false
  )

  behavior of "customQuoteQueryDAO"

  "listAllQuotes" should "return empty if the are no custom quotes for a user id" in {
    val resultFirstUser: Future[Seq[CustomQuotesQuery]] =
      customQuoteQueryDAO.listAllQuotes(userUuid1)
    resultFirstUser.map(_.length shouldBe 0)
  }

  "createQuote" should "add customer quote query in custom_quotations table with user id" in {
    Await.result(customQuoteQueryDAO.createQuote(customQuoteForm, userInfo), 5.seconds)

    val resultFirstUser: Future[Seq[CustomQuotesQuery]] =
      customQuoteQueryDAO.listAllQuotes(userUuid1)
    resultFirstUser.map { response =>
      response.length shouldBe 1
      response.head.quote shouldBe "Demo quote1"
      response.head.author.get shouldBe "User-1"
    }
  }

  it should "add customer quote with author name if the ownQuote parameter is false" in {
    customQuoteQueryDAO.createQuote(
      customQuoteForm.copy(quote = "Demo quote2", ownQuote = false),
      userInfo.copy(userId = userUuid2, name = "User-2")
    )

    val resultFirstUser: Future[Seq[CustomQuotesQuery]] =
      customQuoteQueryDAO.listAllQuotes(userUuid2)
    resultFirstUser.map { response =>
      response.length shouldBe 1
      response.head.author.get shouldBe "Dummy author"
    }
  }

  "listSelectedQuote" should "get the selected quote for logged in user only" in {
    val result: Future[Option[CustomQuotesQuery]] =
      customQuoteQueryDAO.listSelectedQuote(2, userUuid2)
    result.map { response =>
      response.isDefined shouldBe true
      response.get.quote shouldBe "Demo quote2"
    }
  }

  it should "return empty if the user has not that record in the database" in {
    val result: Future[Option[CustomQuotesQuery]] =
      customQuoteQueryDAO.listSelectedQuote(3, userUuid2)
    result.map(_.isDefined shouldBe false)
  }

  "listRandomQuote" should "return any random record for the logged in user only" in {
    val result: Future[Seq[CustomQuotesQuery]] = customQuoteQueryDAO.listRandomQuote(1, userUuid1)
    result.map { response =>
      response.length shouldBe 1
      response.head.userId shouldBe userUuid1
    }
  }

  "deleteQuote" should "delete the selected quote from the table" in {
    customQuoteQueryDAO.createQuote(
      customQuoteForm.copy(quote = "Demo quote3"),
      userInfo.copy(userId = userUuid3, name = "User-3")
    )
    val beforeDelete: Future[Seq[CustomQuotesQuery]] = customQuoteQueryDAO.listAllQuotes(userUuid3)
    beforeDelete.map(_.length shouldBe 1)

    customQuoteQueryDAO.deleteQuote(3, userUuid3)

    val afterDelete: Future[Seq[CustomQuotesQuery]] = customQuoteQueryDAO.listAllQuotes(userUuid3)
    afterDelete.map(_.length shouldBe 0)
  }

  "updateQuote" should "update the existing record for logged iun user" in {
    val updateRecord: Future[Int] =
      customQuoteQueryDAO.updateQuote(
        2,
        userUuid2,
        customQuoteForm.copy(quote = "Updated quote", author = Some("Updated author"))
      )
    updateRecord.map(_ shouldBe 1)
  }

  val result: Future[Option[CustomQuotesQuery]] =
    customQuoteQueryDAO.listSelectedQuote(2, userUuid2)

  result.map { response =>
    response.isDefined shouldBe true
    response.get.quote shouldBe "Updated quote"
    response.get.author.get shouldBe "Updated author"

  }

}
