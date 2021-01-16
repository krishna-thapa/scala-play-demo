package service

import com.krishna.model.{ AllQuotesOfDay, QuotesQuery }
import com.krishna.response.ResponseMsg
import com.krishna.util.DateConversion.getCurrentDate
import com.krishna.util.Logging
import daos.CacheDAO
import play.api.cache.redis.{ CacheApi, SynchronousResult }
import play.api.libs.json.Json
import javax.inject.Inject

class CacheService @Inject()(
    cache: CacheApi,
    cacheDao: CacheDAO
) extends Logging {

  def cacheQuoteOfTheDay(contentDate: String): Either[ResponseMsg, QuotesQuery] = {

    // Get the quote from the content date key from global cache storage in Redis
    cache.get[String](contentDate) match {
      case Some(quote: String) =>
        log.info("Content date found in the cache storage")
        Right(Json.parse(quote).as[QuotesQuery])
      case None =>
        log.info("Content date is not found in the cache storage")
        cacheDao.cacheQuoteOfTheDay(contentDate)
    }
  }

  def getAllCachedQuotes: Either[ResponseMsg, Seq[AllQuotesOfDay]] = {
    // Get all the cached keys(max 5) and will be in date format
    val allCachedKeys: SynchronousResult[Seq[String]] = cache.matching("20*")

    if (allCachedKeys.isEmpty) {
      log.warn("Redis cache is empty, getting quote of the day")
      cacheDao.cacheQuoteOfTheDay(getCurrentDate) match {
        case Left(errorMsg) => Left(errorMsg)
        case Right(_)       => getAllCachedQuotes
      }
    } else {
      log.info("Getting cached quotes from the Redis")
      val quotesOfDay = for {
        (key, index) <- allCachedKeys.zipWithIndex
        quote        <- cache.get[String](key) // Get the stored value for that key from Redis cached storage
      } yield AllQuotesOfDay(key, quote, index)
      Right(quotesOfDay)
    }
  }
}
