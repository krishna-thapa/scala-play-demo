package httpService

import com.krishna.httpService.{ MockedResponse, WebClient }
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.WSResponse

import java.net.URI
import scala.concurrent.Future

/**
  * Mock the response for Wiki Media API
  */
class MockWikiMediaApi extends WebClient {
  override def getWebClientResponse(
      url: String,
      params: Seq[(String, String)],
      headers: Seq[(String, String)]
  ): Future[WSResponse] = {

    val mockedURI: URI = new URI("mocked.url.wiki.api")

    val mockedResponse: JsValue = Json.parse("""
      {
        "title" : "mocked Author",
        "source" : "mocked.api.url.com",
        "description" : ["mocked author description"]
      }
    """)

    Future.successful(new MockedResponse(mockedURI, mockedResponse))
  }
}
