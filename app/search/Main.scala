package search

import com.sksamuel.elastic4s.fields.TextField
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.playjson._
import play.api.libs.json.{ Json, OFormat }
import utils.Logging

// For testing purpose

object Main extends App with Logging {

  // you must import the DSL to use the syntax helpers
  import com.sksamuel.elastic4s.ElasticDsl._

  val props  = ElasticProperties("http://localhost:9200")
  val client = ElasticClient(JavaClient(props))

  // Create an index in advance ready to receive documents.
  // await is a helper method to make this operation synchronous instead of async
  // You would normally avoid doing this in a real program as it will block
  // the calling thread but is useful when testing

  client.execute {
    createIndex("quotes").mapping(
      properties(TextField("text"))
    )
  }.await

  // Next we index a single document which is just the quote.
  // The RefreshPolicy.Immediate means that we want this document to flush to the disk immediately.
  // see the section on Eventual Consistency.
  client.execute {
    indexInto("quotes").fields("text" -> "Hello world").refresh(RefreshPolicy.Immediate)
  }.await

  // now we can search for the document we just indexed
  val searchResult: Response[SearchResponse] = client.execute {
    search("quotes").query("hello")
  }.await

  // searchResult is a Response[+U] ADT consisting of either a RequestFailure containing the
  // Elasticsearch error details, or a RequestSuccess[U] that depends on the type of request.
  // In this case it is a RequestSuccess[SearchResponse]
  log.info("---- Search Results ------")
  searchResult match {
    case failure: RequestFailure => log.error(s"Failed: ${failure.error}")
    case results: RequestSuccess[SearchResponse] =>
      log.info(s"Results: ${results.result.hits.hits.toList}")
  }

  searchResult.foreach(search => log.info(s"There were ${search.totalHits} total hits"))

  // a simple example of a domain model
  case class Character(name: String, location: String)
  implicit val customWrites: OFormat[Character] = Json.format[Character]

  // now index requests can directly use characters as docs
  val jonsnow =
    Seq(Character("jon snow", "the wall"), Character("jon dfgdfgsnow", "thdfgdfge wall"))

  val foo = jonsnow.map(
    foo =>
      client.execute {
        indexInto("gameofthrones").doc(foo)
      }.await
  )

  client.close()
}
