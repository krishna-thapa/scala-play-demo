package config

import play.api.Configuration

trait JwtKey {

  // Logged in user are stored under this key in the play session
  def jwtSessionKey(implicit conf: Configuration): String =
    conf.get[String]("play.http.session.jwtKey")

  // Get the play secret key defined in the app config file.
  // Have to be pass as an environment variable during production
  def secretKey(implicit conf: Configuration): String = conf.get[String]("play.http.secret.key")
}
