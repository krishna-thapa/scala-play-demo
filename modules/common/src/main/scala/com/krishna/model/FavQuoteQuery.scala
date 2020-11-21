package com.krishna.model

import play.api.libs.json.{ Json, OFormat }

final case class FavQuoteQuery(
    id: Int,
    userId: Int,
    csvId: String,
    favTag: Boolean
)

// not needed
object FavQuoteQuery {
  implicit lazy val favQuoteFormat: OFormat[FavQuoteQuery] =
    Json.format[FavQuoteQuery]
}
