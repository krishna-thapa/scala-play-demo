package models

import play.api.libs.json.{ Json, OFormat }

final case class FavQuoteQuery(
    id: Int,
    csvid: String,
    favTag: Boolean
)

// not needed
object FavQuoteQuery {
  implicit lazy val favQuoteFormat: OFormat[FavQuoteQuery] =
    Json.format[FavQuoteQuery]
}
