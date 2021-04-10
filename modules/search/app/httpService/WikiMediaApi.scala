package httpService

import com.krishna.httpService.HttpService
import com.krishna.util.Logging
import javax.inject.{Inject, Singleton}
import models.AuthorDetails
import play.api.Configuration
import responseHandler.EsResponseHandler

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@Singleton
class WikiMediaApi @Inject()(httpService: HttpService, config: Configuration)(
    implicit executionContext: ExecutionContext
) extends Logging {
  private val wikiMediaApiUrl: String = config.get[String]("wiki.apiUrl")

  def getWikiResponse(author: String): Future[AuthorDetails] = {
    val futureResponse: Future[AuthorDetails] = for {
      wsResponse    <- httpService.get(wikiMediaApiUrl.concat(author))
      authorDetails <- EsResponseHandler.validateJson(wsResponse.json.validate[AuthorDetails])
    } yield authorDetails

    futureResponse.andThen {
      case Failure(exception: NoSuchElementException) =>
        log.error(s"Error occurred while json parsing: ${exception.getMessage}")
      case Failure(exception: _) =>
        log.error(s"Error occurred: ${exception.getMessage}")
    }
  }

}
