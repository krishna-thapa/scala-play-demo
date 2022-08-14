package service

import com.krishna.model.AllQuotesOfDay
import com.krishna.model.Genre.Genre
import com.krishna.response.ErrorMsg.{ EmptyDbMsg, InvalidCsvId }
import com.krishna.response.{ ErrorMsg, ResponseResult }
import com.krishna.util.DateConversion.{ convertToDate, getCurrentDate }
import com.krishna.util.Logging
import config.DecodeHeader
import daos.QuoteQueryDAO

import javax.inject.{ Inject, Singleton }
import model.UserDetail
import play.api.Configuration
import play.api.mvc.{ AnyContent, Request, Result }

import java.util.UUID
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.matching.Regex

@Singleton
class QuoteQueryService @Inject() (
  quotesDAO: QuoteQueryDAO,
  cacheService: CacheService,
  favQuoteService: FavQuoteQueryService
)(implicit val ec: ExecutionContext, config: Configuration)
    extends ResponseResult
    with Logging {

  // CsvId should start with "CSV" prefix
  private lazy val csvIdPattern: Regex = "CSV\\d+$".r

  def randomQuoteService(records: Int): Future[Result] = {
    responseOptionResult(quotesDAO.listRandomQuote(records).map(_.headOption))
  }

  def random10QuoteService(records: Int): Future[Result] = {
    log.info("Executing random10QuoteService in QuoteQueryService")
    responseSeqResultAsync(quotesDAO.listRandomQuote(records))
  }

  def quoteOfTheDayService(date: Option[String]): Future[Result] = {
    val contentDate: String =
      date.fold[String](getCurrentDate)((strDate: String) => convertToDate(strDate))
    log.info("Executing quoteOfTheDay for Content Date: " + contentDate)

    responseEitherResult(cacheService.cacheQuoteOfTheDay(contentDate))
  }

  def cachedQuotesService(user: Option[UserDetail]): Future[Result] = {
    log.info("Executing cachedQuotesService method in QuoteQueryService.")
    cacheService.getAllCachedQuotes.flatMap {
      case Left(errorMsg) => responseErrorResult(errorMsg)
      case Right(quotes) =>
        if (user.isEmpty) responseSeqResult(quotes)
        else usersCachedQuotes(quotes, user.get)
    }
  }

  def usersCachedQuotes(quotes: Seq[AllQuotesOfDay], user: UserDetail): Future[Result] = {
    log.info(
      s"Executing usersCachedQuotes in Service for user: ${ user.email } in QuoteQueryService."
    )
    if (quotes.nonEmpty) {
      val cachedFavQuoteIds: Future[Seq[String]] =
        favQuoteService.getFavCachedQuotes(user.userId).map(_.map(_.csvId))
      val futureCachedQuotes: Future[Seq[AllQuotesOfDay]] = cachedFavQuoteIds.map { ids =>
        quotes.map { cachedQuote =>
          if (ids.contains(cachedQuote.quote.csvId)) cachedQuote.copy(isFavQuote = true)
          else cachedQuote
        }
      }
      responseSeqResultAsync(futureCachedQuotes)
    } else responseErrorResult(EmptyDbMsg)
  }

  def allQuotesService(limit: Int, offset: Int): Future[Result] = {
    log.info(s"Executing allQuotesService in Service with limit of $limit and offset of $offset")
    responseSeqResultAsync(quotesDAO.listAllQuotes(limit, offset))
  }

  def updateFavQuoteService(csvId: String, user: UserDetail): Future[Result] = {
    log.info(s"Executing favQuoteService in Service for user: ${ user.email }")
    if (csvIdPattern.matches(csvId)) {
      responseOkAsync(favQuoteService.createOrUpdateFavQuote(user.userId, csvId))
    } else {
      responseErrorResult(InvalidCsvId(csvId))
    }
  }

  def getFavQuotesService(userId: UUID): Future[Result] = {
    log.info(s"Executing getFavQuotesService in Service for userid: $userId")
    responseSeqResultAsync(favQuoteService.listAllQuotes(userId))
  }

  def genreQuoteService(genre: Genre): Future[Result] = {
    log.info(s"Executing genreQuoteService in Service")
    responseOptionResult(quotesDAO.listGenreQuote(genre))
  }

  def searchAuthorsSql(text: String): Future[Seq[String]] = {
    quotesDAO.searchAuthors(text)
  }

  /**
   * Decode Header that returns User details id request has right Auth token
   * @param request With header contents
   * @return Either error message or User details
   */
  def decoderHeader(request: Request[AnyContent]): Either[ErrorMsg, UserDetail] = {
    DecodeHeader(request.headers)
  }

}
