package com.krishna.model

import com.krishna.model.base.IdResource
import play.api.libs.json.{ Json, OFormat }

case class AllQuotesOfDay(
    id: Int,
    contentDate: String,
    quote: QuotesQuery
) extends IdResource

object AllQuotesOfDay {
  implicit lazy val allQuotesOfDay: OFormat[AllQuotesOfDay] =
    Json.format[AllQuotesOfDay]

  def apply(contentDate: String, quote: String, index: Int): AllQuotesOfDay = {
    new AllQuotesOfDay(
      index + 1,
      contentDate,
      Json.parse(quote).as[QuotesQuery]
    )
  }
}