package com.krishna.model

import com.krishna.model.Genre.Genre
import com.krishna.model.base.{ QuotesTable, WithCSCVIdResource }
import play.api.libs.json._

case class QuotesQuery(
    id: Int,
    csvId: String,
    quote: String,
    author: Option[String],
    genre: Option[Genre] = None
) extends WithCSCVIdResource
    with QuotesTable

object QuotesQuery {
  implicit lazy val quotesFormat: OFormat[QuotesQuery] =
    Json.format[QuotesQuery]
}
