package models

import models.Genre.Genre
import play.api.libs.json._

final case class QuotesQuery(
    id: Int,
    csvid: String,
    quote: String,
    author: String,
    genre: Option[Genre] = None
) extends QuotesTable

object QuotesQuery {
  implicit lazy val quotesFormat: OFormat[QuotesQuery] =
    Json.format[QuotesQuery]
}
