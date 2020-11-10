package util

import com.krishna.conf.AppConfig
import model.UserToken
import pdi.jwt.{ JwtAlgorithm, JwtJson }

object DecodeHeader extends AppConfig with JwtKey {

  def apply(token: String): UserToken = {

    val secretKey: String = config.getString("play.http.secret.key")

    // Default Signature algorithm, defined in app config under: play.http.session.algorithm
    (JwtJson.decodeJson(token, secretKey, Seq(JwtAlgorithm.HS256)).get \ jwtSessionKey)
      .as[UserToken]
  }
}
