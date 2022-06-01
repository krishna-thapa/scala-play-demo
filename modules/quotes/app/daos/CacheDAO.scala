package daos

import com.krishna.model.QuotesQuery
import com.krishna.response.ErrorMsg.{ EmptyDbMsg, InvalidDate }
import com.krishna.response.{ ErrorMsg, ResponseError }
import com.krishna.util.DateConversion.getCurrentDate
import play.api.Configuration
import play.api.cache.redis.{ AsynchronousResult, CacheAsyncApi, RedisList, SynchronousResult }
import play.api.libs.json.Json

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.DurationInt

class CacheDAO @Inject() (
  cache: CacheAsyncApi,
  quotesDAO: QuoteQueryDAO,
  config: Configuration,
  implicit val ec: ExecutionContext
) extends ResponseError {

  /*
    A cache API that uses synchronous calls rather than async calls.
    Useful when you know you have a fast in-memory cache.
   */
  protected lazy val quoteOfTheDayCacheList: RedisList[String, AsynchronousResult] =
    cache.list[String]("cache-quoteOfTheDay")

  // TODO: make the max list size to 500
  protected lazy val maxListSize: Int = config.get[Int]("play.cache.storeQuotesSize")

  // Gets a single random quote from the `quotes` table
  private def randomQuote: Future[Option[QuotesQuery]] =
    quotesDAO.listRandomQuote(1).map(_.headOption)

  /**
    * Cache previous 5 days of quote of the day in the Redis storage
    * @param contentDate: To check the date as a key in the Redis
    * @return Response Quote in the JSON
    */
  def cacheQuoteOfTheDay(contentDate: String): Future[Either[ErrorMsg, QuotesQuery]] = {
    if (contentDate.contentEquals(getCurrentDate)) {
      randomQuote.flatMap(
        _.fold[Future[Either[ErrorMsg, QuotesQuery]]](Future(Left(EmptyDbMsg)))(
          (quote: QuotesQuery) => {
            val uniqueQuote: Future[Either[ErrorMsg, QuotesQuery]] =
              getUniqueQuoteFromDB(quote, quoteOfTheDayCacheList)

            // Side effect to store record in the cache storage
            uniqueQuote.foreach { case Right(quote) =>
              log.info("Storing today's quote in the cache storage")
              cache.set(
                key = contentDate,
                value = Json
                  .toJson(quote)
                  .toString, // best way to get the right value from either
                expiration = 5.days // Key is only store for 5 days
              )
            }
            uniqueQuote
          }
        )
      )
    } else {
      log.warn(s"Date has to be within last 5 days: $contentDate")
      Future(Left(InvalidDate(contentDate)))
    }
  }

  /**
    * Recursive method that checks if the quote is present in cache list
    * If it is then retrieve the new quote and call the same method, if not then
    * it updates the redis cache list and return the quote
    * @param quote random quote to check with cache list
    * @param cachedQuotes redis cache list that has all the past called quote csv ids
    * @return random quote that has not been called before
    */
  // TODO: Convert this to tail recursive
  def getUniqueQuoteFromDB(
    quote: QuotesQuery,
    cachedQuotes: RedisList[String, AsynchronousResult]
  ): Future[Either[ErrorMsg, QuotesQuery]] = {

    // Have to covert Redis list to Scala list to use contains method
    // Use of List instead of Set since redis-play has no many wrapper methods for Set and no sorted Set
    cachedQuotes.toList.flatMap { quotes =>
      if (quotes.contains(quote.csvId)) {
        log.warn("Duplicate record has been called with id: " + quote.csvId)
        randomQuote
          .flatMap(
            _.fold[Future[Either[ErrorMsg, QuotesQuery]]](Future(Left(EmptyDbMsg)))(
              (quote: QuotesQuery) => {
                getUniqueQuoteFromDB(quote, cachedQuotes)
              }
            )
          )
      } else {
        redisActions(quote.csvId, cachedQuotes)
        Future(Right(quote))
      }
    }
  }

  /**
    * Unit method that stores the ids in redis list
    * if the list exceeds max length, it deletes the first one and appends to last element
    * @param csvId Unique id of the record
    */
  private def redisActions(
    csvId: String,
    cachedQuotes: RedisList[String, AsynchronousResult]
  ): Unit = {
    cachedQuotes.size.foreach { quotesSize =>
      if (quotesSize >= maxListSize) cachedQuotes.headPop
      cachedQuotes.append(csvId)
      log.info("Ids in the Redis storage: " + cachedQuotes.toList)
    }
  }

}
