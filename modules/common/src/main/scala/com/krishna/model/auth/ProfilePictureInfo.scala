package com.krishna.model.auth

import play.api.libs.json.{Json, OFormat}

case class ProfilePictureInfo(
  filename: String,
  contentType: Option[String],
  fileSize: Long = -1,
  dispositionType: String = "form-data"
)

object ProfilePictureInfo {
  implicit lazy val singUpFormat: OFormat[ProfilePictureInfo] = Json.format[ProfilePictureInfo]
}
