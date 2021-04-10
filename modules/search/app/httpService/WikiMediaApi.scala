package httpService

import com.krishna.httpService.HttpService
import javax.inject.{ Inject, Singleton }
import models.AuthorDetails
import play.api.Configuration
import play.api.libs.json.JsResult

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Failure

@Singleton
class WikiMediaApi @Inject()(httpService: HttpService, config: Configuration)(
    implicit executionContext: ExecutionContext
) {
  private val wikiMediaApiUrl: String = config.get[String]("wiki.apiUrl")

  private def validateJson[T](jsResult: JsResult[T]): Future[T] = {
    if (jsResult.isSuccess) Future.successful(jsResult.get)
    else Future.failed(new NoSuchElementException("Json parse error"))
  }

  def getWikiResponse(author: String): Future[AuthorDetails] = {
    val futureResponse: Future[AuthorDetails] = for {
      wsResponse    <- httpService.get(wikiMediaApiUrl.concat(author))
      authorDetails <- validateJson(wsResponse.json.validate[AuthorDetails])
    } yield authorDetails

    futureResponse.andThen {
      case Failure(exception) =>
        val exceptionData = Map("Error occurred: " -> Seq(exception.getMessage))
        println(exceptionData)
    }
  }

}
