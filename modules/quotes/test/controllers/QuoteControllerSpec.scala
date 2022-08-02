package controllers

import akka.stream.Materializer
import com.krishna.model.Genre
import com.typesafe.config.{ Config, ConfigFactory }
import controllers.quotes.QuoteController
import depInject.SecuredControllerComponents
import model.UserDetail
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout, status, stubPlayBodyParsers }
import service.{ AdminActionBuilder, QuoteQueryService, UserActionBuilder }

import java.sql.Date
import scala.concurrent.{ ExecutionContext, Future }

class QuoteControllerSpec extends PlaySpec with Results with GuiceOneAppPerSuite {
  implicit lazy val materializer: Materializer = app.materializer
  val mockQuoteQueryService: QuoteQueryService = mock[QuoteQueryService]

  val mockSecuredControlledComponents: SecuredControllerComponents =
    mock[SecuredControllerComponents]

  val ec: ExecutionContext = ExecutionContext.Implicits.global
  val conf: Config = ConfigFactory.load()

  val controller =
    new QuoteController(mockQuoteQueryService, mockSecuredControlledComponents)(
      ec,
      Configuration(conf)
    )

  val parse: PlayBodyParsers = stubPlayBodyParsers(materializer)
  val defaultParser: BodyParsers.Default = new BodyParsers.Default(parse)
  val config: Configuration = Configuration.from(Map("play.http.session.jwtKey" -> "mockUser"))

  val mockUserActionBuilder: UserActionBuilder =
    new UserActionBuilder(defaultParser)(ExecutionContext.global, config)

  val mockDefaultActionBuilder: DefaultActionBuilder =
    new DefaultActionBuilderImpl(defaultParser)(ExecutionContext.global)

  val mockAdminActionBuilder: AdminActionBuilder =
    new AdminActionBuilder(defaultParser)(ExecutionContext.global, config)

  val mockUserDetail: UserDetail =
    UserDetail(1, "name", "email@com", new Date(System.currentTimeMillis()), isAdmin = false)

  when(mockSecuredControlledComponents.userActionBuilder).thenReturn(mockUserActionBuilder)
  when(mockSecuredControlledComponents.actionBuilder).thenReturn(mockDefaultActionBuilder)
  when(mockSecuredControlledComponents.adminActionBuilder).thenReturn(mockAdminActionBuilder)

  when(mockQuoteQueryService.randomQuoteService(1))
    .thenReturn(Future.successful(Ok("get-random-test")))

  "QuoteController" should {
    "get the random quote from the Controller" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/random")
      val response = controller.getRandomQuote().apply(request)
      contentAsString(response) mustBe "get-random-test"
      status(response) mustBe 200
    }

    "get the quote of the day from the Controller" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/quoteOfTheDay")
      when(mockQuoteQueryService.quoteOfTheDayService(None))
        .thenReturn(Future.successful(Ok("quote of the day")))
      val response = controller.getQuoteOfTheDay(None).apply(request)
      contentAsString(response) mustBe "quote of the day"
      status(response) mustBe 200
    }

    "get CachedQuotes from the Controller" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/quotesOfTheDay")
      when(mockQuoteQueryService.decoderHeader(any())).thenReturn(Left(any()))
      when(mockQuoteQueryService.cachedQuotesService(None))
        .thenReturn(Future.successful(Ok("CachedQuotes of the day")))
      val response = controller.getCachedQuotes().apply(request)
      contentAsString(response) mustBe "CachedQuotes of the day"
      status(response) mustBe 200
    }

    "get CachedQuotes from the Controller for the logged in user" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/quotesOfTheDay")
      when(mockQuoteQueryService.decoderHeader(any())).thenReturn(Right(mockUserDetail))
      when(mockQuoteQueryService.cachedQuotesService(Some(mockUserDetail)))
        .thenReturn(Future.successful(Ok("CachedQuotes of the day")))
      val response = controller.getCachedQuotes().apply(request)
      contentAsString(response) mustBe "CachedQuotes of the day"
      status(response) mustBe 200
    }

    "get AllQuotes from the Controller without admin role should throw an exception" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/allQuotes")
      when(mockQuoteQueryService.allQuotesService(0, 0))
        .thenReturn(Future.successful(Ok("CachedQuotes of the day")))
      val response = controller.getAllQuotes().apply(request)
      contentAsString(
        response
      ) mustBe "{\"userMsg\":\"Authentication failed: Do not have access!\"}"
    }

    "get the first random 10 quote from the Controller" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/randomTen")
      when(mockQuoteQueryService.random10QuoteService(10))
        .thenReturn(Future.successful(Ok("get-random-test")))
      val response = controller.getFirst10Quotes().apply(request)
      contentAsString(response) mustBe "get-random-test"
      status(response) mustBe 200
    }

    "modify fav quote for the user from the Controller for the logged in user" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/fav/csv123")
      when(mockQuoteQueryService.decoderHeader(any())).thenReturn(Right(mockUserDetail))
      when(mockQuoteQueryService.updateFavQuoteService("csv123", mockUserDetail))
        .thenReturn(Future.successful(Ok("modify fav quotes of the day")))
      val response = controller.favQuote("csv123").apply(request)
      contentAsString(response) mustBe "modify fav quotes of the day"
      status(response) mustBe 200
    }

    "get all fav quotes for the user from the Controller for the logged in user" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/favQuotes")
      when(mockQuoteQueryService.decoderHeader(any())).thenReturn(Right(mockUserDetail))
      when(mockQuoteQueryService.getFavQuotesService(1))
        .thenReturn(Future.successful(Ok("fav quotes of the day")))
      val response = controller.getFavQuotes().apply(request)
      contentAsString(response) mustBe "fav quotes of the day"
      status(response) mustBe 200
    }

    "get genre based quote from the Controller for the logged in user" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/time")
      when(mockQuoteQueryService.genreQuoteService(Genre.time))
        .thenReturn(Future.successful(Ok("genre quote of the day")))
      val response = controller.getGenreQuote(Genre.time).apply(request)
      contentAsString(response) mustBe "genre quote of the day"
      status(response) mustBe 200
    }

    "get autocomplete for author from the Controller for the logged in user" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/abc")
      when(mockQuoteQueryService.searchAuthorsSql("abc"))
        .thenReturn(Future.successful(Seq("abcd", "abcde")))
      val response = controller.getAuthorsAutocomplete("abc").apply(request)
      contentAsString(response) mustBe "[\"abcd\",\"abcde\"]"
      status(response) mustBe 200
    }
  }

}
