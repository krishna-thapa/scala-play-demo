// Should be added controllers for the play routes
package controllers.quotes

import cache.CacheController
import com.krishna.model.Genre.Genre
import com.krishna.response.ResponseMsg.InvalidCsvId
import com.krishna.response.ResponseResult
import com.krishna.util.DateConversion.{ convertToDate, getCurrentDate }
import com.krishna.util.Logging
import daos.{ FavQuoteQueryDAO, QuoteQueryDAO }
import depInject.{ SecuredController, SecuredControllerComponents }
import javax.inject._
import model.UserToken
import play.api.cache.redis.CacheApi
import play.api.libs.json.Json
import play.api.mvc._
import util.DecodeHeader

import scala.concurrent.ExecutionContext
import scala.util.matching.Regex

/**
  * This controller creates an 'Action' to handle HTTP requests to the
  * application's quotes from 'quotes' table.
  */
@Singleton
class QuoteController @Inject()(
    cache: CacheApi,
    cacheController: CacheController,
    scc: SecuredControllerComponents,
    quotesDAO: QuoteQueryDAO,
    favQuotesDAO: FavQuoteQueryDAO
)(implicit executionContext: ExecutionContext)
    extends SecuredController(scc)
    with ResponseResult
    with Logging {

  // CsvId should start with "CSV" prefix
  protected lazy val csvIdPattern: Regex = "CSV[0-9]+$".r

  /**
    * A REST endpoint that gets a random quote as a JSON from quotes table.
    * Should be unique to last 500 retrieved records from this end point
    * Used Redis cache database to store last 500 csv id to get unique record
    * Anyone can do perform this action
    */
  def getRandomQuote: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getRandomQuote")
    responseEitherResult(cacheController.cacheRandomQuote())
  }

  /**
    * A REST endpoint that gets a quote of the day as JSON from quotes table
    *  @param date: Can take milliseconds date format as a path parameter to gets the
    *  previous 5 days quote of the day
    *  It stores the past 5 quote of the day in the Redis cache storage
    *  Anyone can do perform this action
    */
  def getQuoteOfTheDay(date: Option[String]): Action[AnyContent] = Action { implicit request =>
    log.info("Executing getQuoteOfTheDay")

    val contentDate: String =
      date.fold[String](getCurrentDate)((strDate: String) => convertToDate(strDate))
    log.info("Content Date from the API call: " + contentDate)

    // Get the quote from the content date key from global cache storage in Redis
    cache.get[String](contentDate) match {
      case Some(quote: String) =>
        log.info("Content date found in the cache storage")
        Ok(Json.parse(quote))
      case None =>
        log.warn("Content date is not found in the cache storage")
        responseEitherResult(cacheController.cacheQuoteOfTheDay(contentDate))
    }
  }

  def getLastFiveQuotes: Action[AnyContent] = ???

  /**quotations
    * A REST endpoint that gets all the quotes as JSON from quotes table
    * Only Admin can perform this action
    */
  def getAllQuotes: Action[AnyContent] = AdminAction { implicit request =>
    log.info("Executing getAllQuotes")
    responseSeqResult(quotesDAO.listAllQuotes())
  }

  /**
    * A REST endpoint that gets random 10 quotes as JSON from quotes table
    * Anyone can do perform this action
    */
  def getFirst10Quotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getFirst10Quotes")
    responseSeqResult(quotesDAO.listRandomQuote(10))
  }

  /**
    * TODO add user id while saving the fav quote
    * A REST endpoint that creates or altered the fav tag in the fav_quotes table.
    * Only the logged user can perform this action
    */
  def favQuote(csvId: String): Action[AnyContent] = UserAction { implicit request =>
    val user: UserToken = DecodeHeader(request.headers)
    log.info(s"Executing favQuote bu user: ${user.email}")

    if (csvIdPattern.matches(csvId)) {
      responseTryResult(favQuotesDAO.modifyFavQuote(csvId))
    } else {
      badRequest(InvalidCsvId(csvId).msg)
    }
  }

  /**
    * TODO only the logged user should retrieve own fav quotes
    * A REST endpoint that gets all favorite quotes as JSON from fav_quotes table.
    * Only the logged user can perform this action
    */
  def getFavQuotes: Action[AnyContent] = UserAction { implicit request =>
    log.info("Executing getFavQuotes")
    responseSeqResult(favQuotesDAO.listAllQuotes())
  }

  /**
    * A REST endpoint that gets a random quote as per selected genre from the table from quotes table.
    * Returns 400 response code when the invalid genre is used
    * Anyone can do perform this action
    */
  def getGenreQuote(genre: Genre): Action[AnyContent] = Action { implicit request =>
    log.info("Executing getGenreQuote")
    responseOptionResult(quotesDAO.getGenreQuote(genre))
  }
}
