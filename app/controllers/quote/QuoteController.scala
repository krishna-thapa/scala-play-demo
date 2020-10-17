package controllers.quote

import cache.CacheController
import daos.{ FavQuoteQueryDAO, QuoteQueryDAO }
import response.ResponseResult
import javax.inject._
import models.Genre.Genre
import play.api.cache.redis.CacheApi
import play.api.libs.json.Json
import play.api.mvc._
import response.ResponseMsg.InvalidCsvId
import utils.DateConversion._
import utils.Logging

import scala.concurrent.ExecutionContext
import scala.util.matching.Regex

/**
  * This controller creates an 'Action' to handle HTTP requests to the
  * application's quotes from 'quotations' table.
  */
@Singleton
class QuoteController @Inject()(
    cache: CacheApi,
    cacheController: CacheController,
    cc: ControllerComponents,
    quotesDAO: QuoteQueryDAO,
    favQuotesDAO: FavQuoteQueryDAO
)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc)
    with ResponseResult
    with Logging {

  protected lazy val csvIdPattern: Regex = "CSV[0-9]+$".r

  /**
    * A REST endpoint that gets a random quote as JSON from quotations table.
    */
  def getRandomQuote: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getRandomQuote")
    responseEitherResult(cacheController.cacheRandomQuote())
  }

  /**
    * A REST endpoint that gets a quote of the day as JSON from quotations table
    *  @param date: Can take milliseconds date format as a path parameter to gets the
    *  previous 5 days quote of the day
    *  It stores the past 5 quote of the day in the Redis cache storage
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

  /**quotations
    * A REST endpoint that gets all the quotes as JSON from quotations table
    */
  def getAllQuotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getAllQuotes")
    responseSeqResult(quotesDAO.listAllQuotes())
  }

  /**
    * A REST endpoint that gets random 10 quotes as JSON from quotations table
    */
  def getFirst10Quotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getFirst10Quotes")
    responseSeqResult(quotesDAO.listRandomQuote(10))
  }

  /**
    * A REST endpoint that creates or altered the fav tag in the fav_quotes table.
    */
  def favQuote(csvid: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      log.info("Executing favQuote")
      if (csvIdPattern.matches(csvid)) {
        responseTryResult(favQuotesDAO.modifyFavQuote(csvid))
      } else {
        badRequest(InvalidCsvId(csvid).msg)
      }
  }

  /**
    * A REST endpoint that gets all favorite quotes as JSON from fav_quotes table.
    */
  def getFavQuotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getFavQuotes")
    responseSeqResult(favQuotesDAO.listAllQuotes())
  }

  /**
    * A REST endpoint that gets a random quote as per selected genre from the table from quotations table.
    */
  def getGenreQuote(genre: Genre): Action[AnyContent] = Action { implicit request =>
    log.info("Executing getGenreQuote")
    // TODO build response when the genre is invalid type
    responseOptionResult(quotesDAO.getGenreQuote(genre))
  }

  /**
    * A REST endpoint that gets 10 matched autocomplete list from the searched parameter
    */
  def getAuthorsAutocomplete(parameter: String): Action[AnyContent] = Action { implicit request =>
    log.info("Executing getAuthorsAutocomplete")
    responseSeqString(quotesDAO.searchAuthors(parameter))
  }

}
