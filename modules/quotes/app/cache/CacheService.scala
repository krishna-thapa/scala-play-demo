package cache

import com.krishna.model.{ AllQuotesOfDay, QuotesQuery }
import com.krishna.response.ResponseMsg
import com.krishna.util.Logging
import javax.inject.Inject
import play.api.cache.redis.{ CacheApi, SynchronousResult }
import play.api.libs.json.Json

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

  def getCacheRandomQuote: Either[ResponseMsg, QuotesQuery] = {
    cacheDao.cacheRandomQuote()
  }

  def getAllCachedQuotes: Seq[AllQuotesOfDay] = {
    // Get all the cached keys(max 5) and will be in date format
    val allCachedKeys: SynchronousResult[Seq[String]] = cache.matching("20*")
    for {
      key   <- allCachedKeys
      quote <- cache.get[String](key) // Get the stored value for that key from Redis cached storage
    } yield AllQuotesOfDay(key, quote)
  }
}
