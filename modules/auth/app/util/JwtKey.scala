package util

import com.krishna.conf.AppConfig

trait JwtKey extends AppConfig {

  // Logged in user are stored under this key in the play session
  def jwtSessionKey: String = config.getString("play.http.session.jwtKey")
}
