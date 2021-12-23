package httpService

import com.krishna.httpService.HttpService
import com.krishna.util.Logging
import models.AuthorDetails
import play.api.Configuration
import responseHandler.EsResponseHandler

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Failure

@Singleton
class ExtendedWikiMediaApi @Inject()(httpService: HttpService, config: Configuration)(
    implicit executionContext: ExecutionContext
) extends Logging {

  private val wikiMediaApiUrl: String = config.get[String]("wiki.apiUrl")

  /**
    * Call the Wiki Media API and validate the response
    * If the quote doesn't has any author name on it, we can simple return quote only
    * @param quote quote where we can get author name
    * @return quote with author details
    */
  def getWikiResponse(quote: FinalQuotes): Future[FinalQuotesWithAuthor] = {

    quote.author.fold(Future.successful(FinalQuotesWithAuthor(quote))) { author =>
      val futureResponse: Future[FinalQuotesWithAuthor] = for {
        wsResponse <- httpService.getWebClientResponse(
          wikiMediaApiUrl.concat(handleAuthorStr(author))
        )
        authorDetails <- EsResponseHandler.validateJson(wsResponse.json.validate[AuthorDetails])
      } yield FinalQuotesWithAuthor(quote, Some(authorDetails))

      futureResponse.andThen {
        case Failure(exception: NoSuchElementException) =>
          log.error(s"Error occurred while json parsing: ${exception.getMessage}")
        case Failure(exception) =>
          log.error(s"Error occurred: ${exception.getMessage}")
      }
    }
  }

  // Capitalize the word for the author name and surname
  private def handleAuthorStr(author: String): String = {
    author.split(" ").map(_.capitalize).mkString(" ")
  }
}
