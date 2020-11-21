package com.krishna.model

import play.api.libs.json.{ Json, OFormat }

case class AllQuotesOfDay(
    contentDate: String,
    quote: QuotesQuery
)

object AllQuotesOfDay {
  implicit lazy val allQuotesOfDay: OFormat[AllQuotesOfDay] =
    Json.format[AllQuotesOfDay]

  def apply(contentDate: String, quote: String): AllQuotesOfDay = {
    new AllQuotesOfDay(
      contentDate,
      Json.parse(quote).as[QuotesQuery]
    )
  }
}
