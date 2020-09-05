package controllers

import daos.{ FavQuoteQueryDAO, QuoteQueryDAO }
import helper.ResponseMethod
import javax.inject._
import models.Genre.Genre
import models.QuotesQuery
import play.api.mvc._
import utils.Logging
import play.api.cache.redis.{ CacheApi, RedisList, RedisSet, SynchronousResult }

import scala.concurrent.ExecutionContext
import scala.util.matching.Regex
import scala.concurrent.Future
import scala.concurrent.duration._

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

  //val cacheSet: RedisSet[String, SynchronousResult]                  = cache.set[String]("my-set")
  protected lazy val cacheList: RedisList[String, SynchronousResult] = cache.list[String]("my-list")

  /**
    * A REST endpoint that gets a random quote as JSON from CSV quotes table.
    */
  def getRandomQuote: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getRandomQuote")
    val demo: Seq[QuotesQuery] = quotesDAO.listRandomQuote(1)
    redisActions(demo.head.csvid)
    log.info("Cache in the demolist: " + cacheList.toList)
    responseSeqResult(demo)
  }

  def redisActions(csvid: String): Unit = {
    val demolist = cacheList.toList
    if (demolist.size < 5 && !demolist.toList.contains(csvid)) {
      cacheList.append(csvid)
    } else {
      cacheList.removeAt(position = 0)
      cacheList.append(csvid)
    }
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
