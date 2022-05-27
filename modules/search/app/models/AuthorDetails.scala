package models

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{ JsPath, Json, Reads, Writes }

case class AuthorDetails(
  title: String,
  imagerUrl: Option[String],
  description: Option[Seq[String]]
)

object AuthorDetails {

  implicit val readJson: Reads[AuthorDetails] = (
    (JsPath \\ "title").read[String] and
      (JsPath \\ "source").readNullable[String] and
      (JsPath \\ "imagerUrl").readNullable[String] and
      (JsPath \\ "description").readNullable[Seq[String]]
  ).apply((title, sourceUrl, imageUrl, description) =>
    AuthorDetails(title, sourceUrl.orElse(imageUrl), description)
  )

  implicit val writeJson: Writes[AuthorDetails] = Json.writes[AuthorDetails]
}
