package util

import com.krishna.conf.AppConfig
import model.UserDetail
import pdi.jwt.{ JwtAlgorithm, JwtJson }
import play.api.mvc.Headers

object DecodeHeader extends AppConfig with JwtKey {

  def apply(headers: Headers): UserDetail = {

    // already know that header has token in 'Authorization'
    val token: String = headers.get("Authorization").get

    val secretKey: String = config.getString("play.http.secret.key")

    // Default Signature algorithm, defined in app config under: play.http.session.algorithm
    (JwtJson.decodeJson(token, secretKey, Seq(JwtAlgorithm.HS256)).get \ jwtSessionKey)
      .as[UserDetail]
  }
}
