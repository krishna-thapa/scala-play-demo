package models

import play.api.libs.json.{ Json, OFormat }

case class FavQuoteQuery(
    id: Int,
    csvid: String,
    favTag: Boolean
)

object FavQuoteQuery {
  implicit lazy val favQuoteFormat: OFormat[FavQuoteQuery] =
    Json.format[FavQuoteQuery]
}
