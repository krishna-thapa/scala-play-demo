package com.krishna.httpService

import com.krishna.util.Logging
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

/**
  * Generic trait for the Web client that can be use for the DI
  */
trait WebClient extends Logging {

  def getWebClientResponse(
      url: String,
      params: Seq[(String, String)] = Nil,
      headers: Seq[(String, String)] = Nil
  ): Future[WSResponse]
}
