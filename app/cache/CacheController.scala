package cache

import controllers.quote.routes
import helper.ResponseMethod
import javax.inject._
import models.QuotesQuery
import play.api.cache.redis.{ CacheApi, RedisList, SynchronousResult }
import play.api.libs.json.Json
import play.api.mvc.Results.{ Ok, Redirect }
import play.api.mvc._
import utils.DateConversion._
import utils.Logging

import scala.concurrent.duration.DurationInt

class CacheController @Inject()(cache: CacheApi) extends Logging with ResponseMethod {

  // A cache API that uses synchronous calls rather than async calls.
  // Useful when you know you have a fast in-memory cache.
  protected lazy val cacheList: RedisList[String, SynchronousResult] =
    cache.list[String]("cacheIdsList")
  // TODO: make the max list size to 50
  protected lazy val maxListSize: Int = 5

  def cacheQuoteOfTheDay(contentDate: String, randomQuote: Option[QuotesQuery]): Result = {
    if (contentDate.contentEquals(getCurrentDate)) {
      randomQuote.fold(badRequest("Database is empty!"))((quote: QuotesQuery) => {
        log.info("Storing today's quote in the cache storage")
        cache.set(
          key = contentDate,
          value = Json.toJson(quote).toString,
          expiration = 5.days // Key is only store for 5 days
        )
        Ok(Json.toJson(quote))
      })
    } else {
      val errorMsg: String = s"Date has to be within last 5 days: $contentDate"
      badRequest(errorMsg)
    }
  }

  def cacheRandomQuote(csvId: String, quote: QuotesQuery): Result = {
    // Have to covert Redis list to Scala list to use contains method
    if (cacheList.toList.toList.contains(csvId)) {
      log.warn("Duplicate record has been called with id: " + csvId)
      Redirect(routes.QuoteController.getRandomQuote())
    } else {
      redisActions(csvId)
      Ok(Json.toJson(quote))
    }
  }

  /**
    * Unit return method that stores the ids in redis list
    * if the list exceeds max length, it deletes the first one and appends to last element
    * @param csvid Unique id of the record
    */
  private def redisActions(csvid: String): Unit = {
    if (cacheList.size >= maxListSize) cacheList.removeAt(0)
    cacheList.append(csvid)
    log.info("Ids in the Redis storage: " + cacheList.toList)
  }
}
