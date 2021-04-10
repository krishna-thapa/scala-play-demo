package com.krishna.httpService

import play.api.libs.ws.{ WSClient, WSResponse }
import com.krishna.util.Logging
import javax.inject.Inject

import scala.concurrent.Future

class HttpService @Inject()(ws: WSClient) extends Logging {

  def get(
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
