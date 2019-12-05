package models

import play.api.libs.json._

case class QuotesQuery (
  id: Int,
  quote: String,
  author: String,
  genre: String
)

object QuotesQuery {
  implicit val quotesFormat: OFormat[QuotesQuery] = Json.format[QuotesQuery]
}