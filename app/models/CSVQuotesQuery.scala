package models

import models.Genre.Genre
import play.api.libs.json._

case class CSVQuotesQuery(
    id: Int,
    csvid: String,
    quote: String,
    author: String,
    genre: Genre
) extends QuotesTable

object CSVQuotesQuery {
  implicit lazy val quotesFormat: OFormat[CSVQuotesQuery] =
    Json.format[CSVQuotesQuery]
}
