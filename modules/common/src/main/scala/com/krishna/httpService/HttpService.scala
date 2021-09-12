package com.krishna.httpService

import play.api.libs.ws.{ WSClient, WSResponse }

import javax.inject.Inject
import scala.concurrent.Future

/**
  * Generic Http Service class that will have all the HTTP methods
  * @param ws WSClient from Play framework
  */
class HttpService @Inject()(ws: WSClient) extends WebClient {

  def getWebClientResponse(
      url: String,
      params: Seq[(String, String)] = Nil,
      headers: Seq[(String, String)] = Nil
  ): Future[WSResponse] = {

    ws.url(url)
      .withQueryStringParameters(params: _*)
      .withHttpHeaders(headers: _*)
      .withMethod("GET")
      .execute()
  }
}
