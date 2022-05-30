package service

import com.krishna.model.{ AllQuotesOfDay, QuotesQuery }
import com.krishna.response.ErrorMsg
import com.krishna.util.DateConversion.getCurrentDate
import com.krishna.util.Logging
import daos.CacheDAO
import play.api.cache.redis.{ AsynchronousResult, CacheAsyncApi }
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class CacheService @Inject() (
  cache: CacheAsyncApi,
  cacheDao: CacheDAO,
  implicit val ec: ExecutionContext
) extends Logging {

  def cacheQuoteOfTheDay(contentDate: String): Future[Either[ErrorMsg, QuotesQuery]] = {

    // Get the quote from the content date key from global cache storage in Redis
    cache.get[String](contentDate).map {
      case Some(quote: String) =>
        log.info("Content date found in the cache storage")
        Right(Json.parse(quote).as[QuotesQuery])
      case None =>
        log.info("Content date is not found in the cache storage")
        cacheDao.cacheQuoteOfTheDay(contentDate)
    }
  }

  def getAllCachedQuotes: Future[Either[ErrorMsg, Seq[AllQuotesOfDay]]] = {
    // Get all the cached keys(max 5) and will be in date format
    val allCachedKeys: AsynchronousResult[Seq[String]] = cache.matching("20*")

    allCachedKeys.map { cachedKeys =>
      if (cachedKeys.isEmpty) {
        log.warn("Redis cache is empty, getting quote of the day")
        cacheDao.cacheQuoteOfTheDay(getCurrentDate) match {
          case Left(errorMsg) => Left(errorMsg)
          case Right(_)       => getAllCachedQuotes
        }
      } else {
        log.info("Getting cached quotes from the Redis")
        val quotesOfDay: Seq[Nothing] = for {
          (key, index) <- cachedKeys.zipWithIndex
          // Get the stored value for that key from Redis cached storage
          quote <- cache.get[String](key)
        } yield AllQuotesOfDay(key, quote, index)
        Right(quotesOfDay)
      }
    }
  }

}
