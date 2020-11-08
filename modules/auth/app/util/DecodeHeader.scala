package util

import com.typesafe.config.{ Config, ConfigFactory }
import model.UserToken
import pdi.jwt.{ JwtAlgorithm, JwtJson }

object DecodeHeader {

  def config: Config = ConfigFactory.load("application")

  def apply(token: String): UserToken = {

    val secretKey: String = config.getString("play.http.secret.key")

    // Default Signature algorithm, defined in app config under: play.http.session.algorithm
    (JwtJson.decodeJson(token, secretKey, Seq(JwtAlgorithm.HS256)).get \ "user").as[UserToken]
  }
}
