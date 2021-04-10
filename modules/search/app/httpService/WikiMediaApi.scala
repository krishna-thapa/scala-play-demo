package httpService

import com.krishna.httpService.HttpService
import com.krishna.model.QuotesQuery
import com.krishna.util.Logging
import javax.inject.{ Inject, Singleton }
import models.{ AuthorDetails, QuoteWithAuthor }
import play.api.Configuration
import responseHandler.EsResponseHandler

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Failure

@Singleton
class WikiMediaApi @Inject()(httpService: HttpService, config: Configuration)(
    implicit executionContext: ExecutionContext
) extends Logging {
  private val wikiMediaApiUrl: String = config.get[String]("wiki.apiUrl")

  def getWikiResponse(quote: QuotesQuery): Future[QuoteWithAuthor] = {

    quote.author.fold(Future.successful(QuoteWithAuthor(quote))) { author =>
      val futureResponse: Future[QuoteWithAuthor] = for {
        wsResponse    <- httpService.get(wikiMediaApiUrl.concat(handleAuthorStr(author)))
        authorDetails <- EsResponseHandler.validateJson(wsResponse.json.validate[AuthorDetails])
      } yield QuoteWithAuthor(quote, Some(authorDetails))

      futureResponse.andThen {
        case Failure(exception: NoSuchElementException) =>
          log.error(s"Error occurred while json parsing: ${exception.getMessage}")
        case Failure(exception) =>
          log.error(s"Error occurred: ${exception.getMessage}")
      }
    }
  }

  def handleAuthorStr(author: String): String = {
    author.split(" ").map(_.capitalize).mkString(" ")
  }
}
