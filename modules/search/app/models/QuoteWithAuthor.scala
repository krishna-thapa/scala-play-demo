package models

import com.krishna.model.QuotesQuery
import com.sksamuel.elastic4s.ElasticDsl.indexInto
import com.sksamuel.elastic4s.playjson.playJsonIndexable
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.streams.RequestBuilder
import play.api.libs.json.{ Json, OFormat }

trait QuoteAuthor[T] {
  def quoteDetails: T
}

case class QuoteWithAuthor(
    quoteDetails: QuotesQuery,
    authorDetails: Option[AuthorDetails] = None
) extends QuoteAuthor[QuotesQuery]

case class Author(author: String)
case class AuthorSearchResponse(quoteDetails: Author) extends QuoteAuthor[Author]

object QuoteAuthor {

  implicit lazy val authorFormat: OFormat[Author] = Json.format[Author]
  implicit lazy val authorSearchFormat: OFormat[AuthorSearchResponse] =
    Json.format[AuthorSearchResponse]
  implicit lazy val quoteWithAuthorFormat: OFormat[QuoteWithAuthor] =
    Json.format[QuoteWithAuthor]
}

object QuoteWithAuthor {
  /*
    An implementation of RequestBuilder to load stream in ElasticSearch
   */
  def builder(indexName: String): RequestBuilder[QuoteWithAuthor] = { (quote: QuoteWithAuthor) =>
    indexInto(indexName).id(quote.quoteDetails.csvId).doc(quote).refresh(RefreshPolicy.Immediate)
  }
}
