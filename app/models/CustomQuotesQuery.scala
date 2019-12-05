package models

import java.sql.Date

import play.api.libs.json._

case class CustomQuotesQuery (
  id: Int,
  quote: String,
  author: String,
  genre: String,
  storedDate: Date,
  ownQuote: Boolean
) extends QuotesTable {

  def hasGenre: Boolean = genre.trim.nonEmpty

}

object CustomQuotesQuery {
  implicit lazy val customerQuotesFormat: OFormat[CustomQuotesQuery] = Json.format[CustomQuotesQuery]
}
