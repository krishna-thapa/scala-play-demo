package models

import play.api.libs.json._

case class QuotesQuery (
  id: Int,
  quote: String,
  author: String,
  genre: String
) extends QuotesTable {

  def hasGenre: Boolean = genre.trim.nonEmpty
}

object QuotesQuery {
  implicit lazy val quotesFormat: OFormat[QuotesQuery] = Json.format[QuotesQuery]
}