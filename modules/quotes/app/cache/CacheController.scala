package cache

import com.krishna.model.QuotesQuery
import com.krishna.response.ResponseMsg.{ EmptyDbMsg, InvalidDate }
import com.krishna.response.{ ResponseError, ResponseMsg }
import com.krishna.util.DateConversion.getCurrentDate
import com.krishna.util.Logging
import daos.QuoteQueryDAO
import javax.inject._
import play.api.cache.redis.{ CacheApi, RedisList, SynchronousResult }
import play.api.libs.json.Json

import scala.concurrent.duration.DurationInt

class CacheController @Inject()(cache: CacheApi, quotesDAO: QuoteQueryDAO)
    extends Logging
    with ResponseError {

  /*
    A cache API that uses synchronous calls rather than async calls.
    Useful when you know you have a fast in-memory cache.
   */
  protected lazy val randomQuoteCacheList: RedisList[String, SynchronousResult] =
    cache.list[String]("cache-random-quote")

  protected lazy val quoteOfTheDayCacheList: RedisList[String, SynchronousResult] =
    cache.list[String]("cache-quoteOfTheDay")

  // TODO: make the max list size to 50
  protected lazy val maxListSize: Int = 5

  private def randomQuote: Option[QuotesQuery] = quotesDAO.listRandomQuote(1).headOption

  /**
    * Cache previous 5 days of quote of the day in the Redis storage
    * @param contentDate: To check the date as a key in the Redis
    * @return Response Quote in the JSON
    */
  def cacheQuoteOfTheDay(contentDate: String): Either[ResponseMsg, QuotesQuery] = {
    if (contentDate.contentEquals(getCurrentDate)) {
      randomQuote.fold[Either[ResponseMsg, QuotesQuery]](Left(EmptyDbMsg))((quote: QuotesQuery) => {
        val uniqueQuote: Either[ResponseMsg, QuotesQuery] =
          getUniqueQuoteFromDB(quote, quoteOfTheDayCacheList)

        // Side effect to store the cache storage
        if (uniqueQuote.isRight) {
          log.info("Storing today's quote in the cache storage")
          cache.set(
            key = contentDate,
            value = Json.toJson(uniqueQuote.toOption.get).toString, // best way to get the right value from either
            expiration = 5.days                                     // Key is only store for 5 days
          )
        }

        uniqueQuote
      })
    } else {
      log.warn(s"Date has to be within last 5 days: $contentDate")
      Left(InvalidDate(contentDate))
    }
  }

  def getUniqueQuoteFromDB(
      quote: QuotesQuery,
      cachedQuotes: RedisList[String, SynchronousResult]
  ): Either[ResponseMsg, QuotesQuery] = {

    // Have to covert Redis list to Scala list to use contains method
    if (cachedQuotes.toList.toList.contains(quote.csvid)) {
      log.warn("Duplicate record has been called with id: " + quote.csvid)
      randomQuote
        .fold[Either[ResponseMsg, QuotesQuery]](Left(EmptyDbMsg))((quote: QuotesQuery) => {
          getUniqueQuoteFromDB(quote, cachedQuotes)
        })
    } else {
      redisActions(quote.csvid, cachedQuotes)
      Right(quote)
    }
  }

  /**
    * Cache storage for the random quote API
    * First 500 response should be unique quote
    * @return the quote in the JSON format
    */
  def cacheRandomQuote(): Either[ResponseMsg, QuotesQuery] = {
    randomQuote.fold[Either[ResponseMsg, QuotesQuery]](Left(EmptyDbMsg))((quote: QuotesQuery) => {
      getUniqueQuoteFromDB(quote, randomQuoteCacheList)
    })
  }

  /**
    * Unit return method that stores the ids in redis list
    * if the list exceeds max length, it deletes the first one and appends to last element
    * @param csvid Unique id of the record
    */
  private def redisActions(
      csvid: String,
      cachedQuotes: RedisList[String, SynchronousResult]
  ): Unit = {
    if (cachedQuotes.size >= maxListSize) cachedQuotes.removeAt(0)
    cachedQuotes.append(csvid)
    log.info("Ids in the Redis storage: " + cachedQuotes.toList)
  }
}
