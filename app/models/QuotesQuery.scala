package models

import play.api.libs.json._

case class QuotesQuery (
  index: Int,
  author: String,
  quote: String
)

object QuotesQuery {
  implicit val quotesFormat: OFormat[QuotesQuery] = Json.format[QuotesQuery]
}