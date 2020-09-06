package controllers.quote

import daos.{ FavQuoteQueryDAO, QuoteQueryDAO }
import helper.ResponseMethod
import javax.inject._
import models.Genre.Genre
import models.QuotesQuery
import play.api.cache.redis.{ CacheApi, RedisList, SynchronousResult }
import play.api.libs.json.Json
import play.api.mvc._
import utils.Logging

import scala.concurrent.ExecutionContext
import scala.util.matching.Regex

/**
  * This controller creates an 'Action' to handle HTTP requests to the
  * application's quotes from CSV Query table.
  */
@Singleton
class QuoteController @Inject()(
    cache: CacheApi,
    cc: ControllerComponents,
    quotesDAO: QuoteQueryDAO,
    favQuotesDAO: FavQuoteQueryDAO
)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc)
    with ResponseMethod
    with Logging {

  protected lazy val csvIdPattern: Regex = "CSV[0-9]+$".r

  // A cache API that uses synchronous calls rather than async calls.
  // Useful when you know you have a fast in-memory cache.
  protected lazy val cacheList: RedisList[String, SynchronousResult] =
    cache.list[String]("cacheIdsList")
  // TODO: make the max list size to 500
  protected lazy val maxListSize: Int = 5

  /**
    * A REST endpoint that gets a random quote as JSON from CSV quotes table.
    */
  def getRandomQuote: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getRandomQuote")
    val getRandomQuery: Seq[QuotesQuery] = quotesDAO.listRandomQuote(1)
    log.info("ID of the record: " + getRandomQuery.head.csvid)
    if (getRandomQuery.nonEmpty) {
      if (cacheList.toList.toList.contains(getRandomQuery.head.csvid)) {
        log.warn("Duplicate record has been called with id: " + getRandomQuery.head.csvid)
        //Ok(Json.toJson(getRandomQuery))
        Redirect(routes.QuoteController.getRandomQuote())
      } else {
        redisActions(getRandomQuery.head.csvid)
        Ok(Json.toJson(getRandomQuery))
      }
    } else {
      notFound("Database is empty!")
    }
  //responseSeqResult(getRandomQuery)
  }

  /**
    * Unit return method that stores the ids in redis list
    * if the list exceeds max length, it deletes the first one and appends to last element
    * @param csvid Unique id of the record
    */
  private def redisActions(csvid: String): Unit = {
    // && !demolist.toList.contains(csvid)
    if (cacheList.size >= maxListSize) cacheList.removeAt(0)
    cacheList.append(csvid)
    log.info("Ids in the Redis storage: " + cacheList.toList)
  }

  /**
    * A REST endpoint that gets all the quotes as JSON from CSV quotes table.
    */
  def getAllQuotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getAllQuotes")
    responseSeqResult(quotesDAO.listAllQuotes())
  }

  /**
    * A REST endpoint that gets random 10 quotes as JSON from CSV quotes table.
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
        badRequest("Id of quote should be in CSV123 format!")
      }
  }

  /**
    * A REST endpoint that gets all favorite quotes as JSON.
    */
  def getFavQuotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getFavQuotes")
    responseSeqResult(favQuotesDAO.listAllQuotes())
  }

  /**
    * A REST endpoint that gets a random quote as per selected genre from the table.
    */
  def getGenreQuote(genre: Genre): Action[AnyContent] = Action { implicit request =>
    log.info("Executing getGenreQuote")
    responseOptionResult(quotesDAO.getGenreQuote(genre))
  }

}
