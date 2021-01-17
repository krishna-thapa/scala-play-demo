package service

import com.krishna.model.AllQuotesOfDay
import com.krishna.model.Genre.Genre
import com.krishna.response.ErrorMsg.{ EmptyDbMsg, InvalidCsvId }
import com.krishna.response.ResponseResult
import com.krishna.util.DateConversion.{ convertToDate, getCurrentDate }
import com.krishna.util.Logging
import daos.QuoteQueryDAO
import javax.inject.{ Inject, Singleton }
import model.UserDetail
import play.api.mvc.Result

import scala.util.matching.Regex

@Singleton
class QuoteQueryService @Inject()(
    quotesDAO: QuoteQueryDAO,
    cacheService: CacheService,
    favQuoteService: FavQuoteQueryService
) extends ResponseResult
    with Logging {

  // CsvId should start with "CSV" prefix
  private lazy val csvIdPattern: Regex = "CSV[0-9]+$".r

  def randomQuoteService(records: Int): Result = {
    log.info("Executing randomQuoteService in Service")
    responseSeqResult(quotesDAO.listRandomQuote(records))
  }

  def quoteOfTheDayService(date: Option[String]): Result = {
    log.info("Executing quoteOfTheDayService in Service")
    val contentDate: String =
      date.fold[String](getCurrentDate)((strDate: String) => convertToDate(strDate))
    log.info("Content Date from the API call: " + contentDate)

    responseEitherResult(cacheService.cacheQuoteOfTheDay(contentDate))
  }

  def cachedQuotesService(user: Option[UserDetail]): Result = {
    log.info("Executing cachedQuotesService in Service")
    cacheService.getAllCachedQuotes match {
      case Left(errorMsg) => responseErrorResult(errorMsg)
      case Right(quotes) =>
        if (user.isEmpty) responseSeqResult(quotes) else usersCachedQuotes(quotes, user.get)
    }
  }

  def usersCachedQuotes(quotes: Seq[AllQuotesOfDay], user: UserDetail): Result = {
    log.info(s"Executing usersCachedQuotes in Service for user: ${user.email}")
    if (quotes.nonEmpty) {
      val cachedFavQuoteIds: Seq[String] =
        favQuoteService.getFavCachedQuotes(user.id).map(_.csvId)
      responseSeqResult(quotes.map { cachedQuote =>
        if (cachedFavQuoteIds.contains(cachedQuote.quote.csvId))
          cachedQuote.copy(isFavQuote = true)
        else cachedQuote
      })
    } else notFound(EmptyDbMsg.msg)
  }

  def allQuotesService(): Result = {
    log.info("Executing allQuotesService in Service")
    responseSeqResult(quotesDAO.listAllQuotes)
  }

  def updateFavQuoteService(csvId: String, user: UserDetail): Result = {
    log.info(s"Executing favQuoteService in Service for user: ${user.email}")
    if (csvIdPattern.matches(csvId)) {
      responseTryResult(favQuoteService.createOrUpdateFavQuote(user.id, csvId))
    } else {
      responseErrorResult(InvalidCsvId(csvId))
    }
  }

  def getFavQuotesService(userId: Int): Result = {
    log.info(s"Executing getFavQuotesService in Service for userid: $userId")
    responseSeqResult(favQuoteService.listAllQuotes(userId))
  }

  def genreQuoteService(genre: Genre): Result = {
    log.info(s"Executing genreQuoteService in Service")
    responseOptionResult(quotesDAO.listGenreQuote(genre))
  }
}
