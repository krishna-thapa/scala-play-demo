package com.krishna.model

import com.krishna.model.base.WithCSCVIdResource
import play.api.libs.json.{ Json, OFormat }

final case class FavQuoteQuery(
    id: Int,
    csvId: String,
    userId: Int,
    favTag: Boolean
) extends WithCSCVIdResource

// not needed
object FavQuoteQuery {
  implicit lazy val favQuoteFormat: OFormat[FavQuoteQuery] =
    Json.format[FavQuoteQuery]
}
