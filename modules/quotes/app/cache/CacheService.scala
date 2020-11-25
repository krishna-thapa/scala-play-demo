package cache

import com.krishna.model.QuotesQuery
import com.krishna.response.ResponseMsg
import com.krishna.util.Logging
import javax.inject.Inject
import play.api.cache.redis.CacheApi
import play.api.libs.json.Json

class CacheService @Inject()(
    cache: CacheApi,
    cacheController: CacheController
) extends Logging {

  def cacheQuoteOfTheDay(contentDate: String): Either[ResponseMsg, QuotesQuery] = {
    cache.get[String](contentDate) match {
      case Some(quote: String) =>
        log.info("Content date found in the cache storage")
        Right(Json.parse(quote).as[QuotesQuery])
      case None =>
        log.warn("Content date is not found in the cache storage")
        cacheController.cacheQuoteOfTheDay(contentDate)
    }
  }
}
