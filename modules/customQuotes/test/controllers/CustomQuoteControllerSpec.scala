package controllers

import akka.stream.Materializer
import com.krishna.model.Genre
import controllers.customQuotes.CustomQuoteController
import dao.CustomQuoteQueryDAO
import depInject.SecuredControllerComponents
import forms.RequestForm
import forms.RequestForm.CustomQuoteForm
import model.UserDetail
import models.CustomQuotesQuery
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.data.{ DefaultFormBinding, Form }
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.UserActionBuilder

import java.sql.Date
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

class CustomQuoteControllerSpec extends PlaySpec with Results with GuiceOneAppPerSuite {

  implicit lazy val materializer: Materializer = app.materializer

  val mockCustomQuoteQueryDAO: CustomQuoteQueryDAO = mock[CustomQuoteQueryDAO]

  val mockSecuredControlledComponents: SecuredControllerComponents =
    mock[SecuredControllerComponents]

  val customQuoteController: CustomQuoteController = new CustomQuoteController(
    mockSecuredControlledComponents,
    mockCustomQuoteQueryDAO
  )

  val parse: PlayBodyParsers = stubPlayBodyParsers(materializer)
  val defaultParser: BodyParsers.Default = new BodyParsers.Default(parse)
  val config: Configuration = Configuration.from(Map("play.http.session.jwtKey" -> "mockUser"))

  val mockUserActionBuilder: UserActionBuilder =
    new UserActionBuilder(defaultParser)(ExecutionContext.global, config)

  when(mockSecuredControlledComponents.userActionBuilder).thenReturn(mockUserActionBuilder)

  val mockRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withHeaders(("Authorization", "1"))

  val userUuid: UUID = UUID.randomUUID()

  val mockUserDetail: UserDetail =
    UserDetail(userUuid, "name", "email@com", new Date(System.currentTimeMillis()), isAdmin = false)

  val mockCustomQuote: CustomQuotesQuery = CustomQuotesQuery(
    1,
    userUuid,
    "dummy Quote",
    None,
    Some(Genre.cool),
    Date.valueOf("2015-03-01"),
    ownQuote = false
  )

  when(mockCustomQuoteQueryDAO.decoderHeader(mockRequest)).thenReturn(Right(mockUserDetail))

  "CustomQuoteController" should {
    "getCustomQuotes should give all the custom quotes only" in {
      when(mockCustomQuoteQueryDAO.listAllQuotes(userUuid))
        .thenReturn(Future.successful(Seq(mockCustomQuote)))
      val result: Future[Result] = customQuoteController.getCustomQuotes.apply(mockRequest)
      val bodyText: String = contentAsString(result)
      bodyText mustBe
        s"""[{"id":1,"userId":"$userUuid","quote":"dummy Quote","genre":"cool","storedDate":1425168000000,"ownQuote":false}]""".stripMargin
    }

    "getRandomCustomQuote should give random quote only" in {
      when(mockCustomQuoteQueryDAO.listRandomQuote(1, userUuid))
        .thenReturn(Future.successful(Seq(mockCustomQuote)))
      val result: Future[Result] = customQuoteController.getRandomCustomQuote.apply(mockRequest)
      val bodyText: String = contentAsString(result)
      bodyText mustBe
        s"""[{"id":1,"userId":"$userUuid","quote":"dummy Quote","genre":"cool","storedDate":1425168000000,"ownQuote":false}]""".stripMargin
    }

    "getSelectedQuote should give selected quote only" in {
      when(mockCustomQuoteQueryDAO.listSelectedQuote(1, userUuid))
        .thenReturn(Future.successful(Some(mockCustomQuote)))
      val result: Future[Result] = customQuoteController.getSelectedQuote(1).apply(mockRequest)
      val bodyText: String = contentAsString(result)
      bodyText mustBe
        s"""{"id":1,"userId":"$userUuid","quote":"dummy Quote","genre":"cool","storedDate":1425168000000,"ownQuote":false}""".stripMargin
    }

    "deleteCustomQuote should delete selected quote only" in {
      when(mockCustomQuoteQueryDAO.deleteQuote(1, userUuid)).thenReturn(Future.successful(1))
      val result: Future[Result] = customQuoteController.deleteCustomQuote(1).apply(mockRequest)
      val bodyText: String = contentAsString(result)
      bodyText mustBe
        """{"success":"Successfully delete quote with id: 1"}""".stripMargin
    }

    // TODO add more test for the Create and Update Controller methods
    "addCustomQuote should insert a new quote" in {
      when(mockCustomQuoteQueryDAO.createQuote(any(), any()))
        .thenReturn(Future.successful(mockCustomQuote))
      when(mockCustomQuoteQueryDAO.decoderHeader(any()))
        .thenReturn(Right(mockUserDetail))
      when(mockSecuredControlledComponents.parsers).thenReturn(mock[DefaultPlayBodyParsers])
      when(mock[DefaultPlayBodyParsers].formBinding(any(), any()))
        .thenReturn(mock[DefaultFormBinding])

      val res: Form[CustomQuoteForm] =
        RequestForm.quotesQueryForm.bind(Map("quote" -> "Random quote"))

      val result: Future[Result] =
        customQuoteController
          .addCustomQuote()
          .apply(
            mockRequest.withJsonBody(
              Json.parse("""
                           |{
                           |    "quote" : "mock-quote",
                           |    "author" : "mock-author",
                           |    "genre" : "age",
                           |    "ownQuote" : true
                           |}
                           |""".stripMargin)
            )
          )
      val bodyText: String = contentAsString(result)
      bodyText mustBe ""
    }
  }

}
