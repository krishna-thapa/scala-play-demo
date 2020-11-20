package models

import java.sql.Date

import com.krishna.model.QuotesTable
import com.krishna.model.Genre.Genre
import play.api.libs.json._

// https://nrinaudo.github.io/scala-best-practices/tricky_behaviours/final_case_classes.html
final case class CustomQuotesQuery(
    id: Int,
    quote: String,
    author: String,
    genre: Option[Genre] = None,
    storedDate: Date,
    ownQuote: Boolean
) extends QuotesTable

object CustomQuotesQuery {
  implicit lazy val customerQuotesFormat: OFormat[CustomQuotesQuery] =
    Json.format[CustomQuotesQuery]
}
