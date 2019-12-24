package models

import java.sql.Date

import play.api.libs.json._

case class CustomQuotesQuery (
  id: Int,
  quote: String,
  author: String,
  genre: String,
  storeddate: Date,
  ownquote: Boolean
) extends QuotesTable

object CustomQuotesQuery {
  implicit lazy val customerQuotesFormat: OFormat[CustomQuotesQuery] = Json.format[CustomQuotesQuery]
}
