package models

import java.sql.Date

import models.Genre.Genre
import play.api.libs.json._

case class CustomQuotesQuery(
    id: Int,
    quote: String,
    author: String,
    genre: Option[Genre] = None,
    storeddate: Date,
    ownquote: Boolean
) extends QuotesTable

object CustomQuotesQuery {
  implicit lazy val customerQuotesFormat: OFormat[CustomQuotesQuery] =
    Json.format[CustomQuotesQuery]
}
