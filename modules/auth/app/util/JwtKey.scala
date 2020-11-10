package util

import com.krishna.conf.AppConfig

trait JwtKey extends AppConfig {

  def jwtSessionKey: String = config.getString("play.http.session.jwtKey")
}
