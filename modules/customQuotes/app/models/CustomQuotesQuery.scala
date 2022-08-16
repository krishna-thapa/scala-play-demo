package models

import com.krishna.model.Genre.Genre
import com.krishna.model.base.{ QuotesTable, WithUserIdResource }
import play.api.libs.json._

import java.sql.Date
import java.util.UUID

// https://nrinaudo.github.io/scala-best-practices/tricky_behaviours/final_case_classes.html
final case class CustomQuotesQuery(
  id: Int,
  userId: UUID,
  quote: String,
  author: Option[String],
  genre: Option[Genre] = None,
  storedDate: Date,
  ownQuote: Boolean
) extends QuotesTable
    with WithUserIdResource

object CustomQuotesQuery {

  implicit lazy val customerQuotesFormat: OFormat[CustomQuotesQuery] =
    Json.format[CustomQuotesQuery]

}
