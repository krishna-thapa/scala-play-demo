package controllers

import akka.stream.Materializer
import com.krishna.model.Genre
import controllers.customQuotes.CustomQuoteController
import dao.CustomQuoteQueryDAO
import depInject.SecuredControllerComponents
import model.UserDetail
import models.CustomQuotesQuery
import org.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.UserActionBuilder

import java.sql.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

class CustomQuoteControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite {

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

  val mockUserDetail: UserDetail =
    UserDetail(1, "name", "email@com", new Date(System.currentTimeMillis()), isAdmin = false)

  val mockCustomQuote: CustomQuotesQuery = CustomQuotesQuery(
    1,
    1,
    "dummy Quote",
    None,
    Some(Genre.cool),
    Date.valueOf("2015-03-01"),
    ownQuote = false
  )

  when(mockCustomQuoteQueryDAO.decoderHeader(mockRequest)).thenReturn(Right(mockUserDetail))

  "CustomQuoteController" should {
    "getCustomQuotes sgould give all the custom quotes only" in {
      when(mockCustomQuoteQueryDAO.listAllQuotes(1))
        .thenReturn(Seq(mockCustomQuote))
      val result: Future[Result] = customQuoteController.getCustomQuotes.apply(mockRequest)
      val bodyText: String = contentAsString(result)
      bodyText mustBe
        """[{"id":1,"userId":1,"quote":"dummy Quote","genre":"cool","storedDate":1425168000000,"ownQuote":false}]""".stripMargin
    }

    "getRandomCustomQuote should give random quote only" in {
      when(mockCustomQuoteQueryDAO.listRandomQuote(1, 1))
        .thenReturn(Seq(mockCustomQuote))
      val result: Future[Result] = customQuoteController.getRandomCustomQuote.apply(mockRequest)
      val bodyText: String = contentAsString(result)
      bodyText mustBe
        """[{"id":1,"userId":1,"quote":"dummy Quote","genre":"cool","storedDate":1425168000000,"ownQuote":false}]""".stripMargin
    }

    "getSelectedQuote should give selected quote only" in {
      when(mockCustomQuoteQueryDAO.listSelectedQuote(1, 1)).thenReturn(Some(mockCustomQuote))
      val result: Future[Result] = customQuoteController.getSelectedQuote(1).apply(mockRequest)
      val bodyText: String = contentAsString(result)
      bodyText mustBe
        """{"id":1,"userId":1,"quote":"dummy Quote","genre":"cool","storedDate":1425168000000,"ownQuote":false}""".stripMargin
    }

    "deleteCustomQuote should delete selected quote only" in {
      when(mockCustomQuoteQueryDAO.deleteQuote(1, 1)).thenReturn(1)
      val result: Future[Result] = customQuoteController.deleteCustomQuote(1).apply(mockRequest)
      val bodyText: String = contentAsString(result)
      bodyText mustBe
        """{"success":"Successfully delete quote with id: 1"}""".stripMargin
    }

    // TODO add more test for the Create and Update Controller methods
    "addCustomQuote should insert a new quote" in {}
  }

}
