package com.krishna.httpService

import akka.stream.scaladsl.Source
import akka.util.ByteString
import play.api.libs.json.JsValue
import play.api.libs.ws.{ WSCookie, WSResponse }

import java.net.URI
import scala.xml.Elem

/**
  * Generic Mock Response class to send for any mock API call
  * @param url mocked URI
  * @param mockedResponse mocked Response from API call
  */
class MockedResponse(url: URI, mockedResponse: JsValue) extends WSResponse {
  override def status: Int = 200

  override def statusText: String = ""

  override def headers: Map[String, collection.Seq[String]] = Map.empty

  override def underlying[T]: T = ???

  override def cookies: collection.Seq[WSCookie] = Seq.empty

  override def cookie(name: String): Option[WSCookie] = None

  override def body: String = ""

  override def bodyAsBytes: ByteString = null

  override def bodyAsSource: Source[ByteString, _] = null

  override def allHeaders: Map[String, collection.Seq[String]] = Map.empty

  override def xml: Elem = null

  override def json: JsValue = mockedResponse

  override def uri: URI = url
}
