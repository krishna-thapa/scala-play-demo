package com.krishna.model

import com.krishna.model.Genre.Genre
import play.api.libs.json._

final case class QuotesQuery(
    id: Int,
    csvId: String,
    quote: String,
    author: String,
    genre: Option[Genre] = None
) extends QuotesTable

object QuotesQuery {
  implicit lazy val quotesFormat: OFormat[QuotesQuery] =
    Json.format[QuotesQuery]
}
