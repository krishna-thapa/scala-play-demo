package models

import play.api.libs.json._

case class CSVQuotesQuery(
  id: Int,
  quote: String,
  author: String,
  genre: String
) extends QuotesTable

object CSVQuotesQuery {
  implicit lazy val quotesFormat: OFormat[CSVQuotesQuery] = Json.format[CSVQuotesQuery]
}