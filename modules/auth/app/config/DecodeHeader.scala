package config

import com.krishna.conf.AppConfig
import com.krishna.response.ErrorMsg
import com.krishna.response.ErrorMsg.{ NoAuthorizationField, TokenDecodeFailure }
import model.UserDetail
import pdi.jwt.{ JwtAlgorithm, JwtJson }
import play.api.mvc.Headers

import scala.util.{ Failure, Success }

object DecodeHeader extends AppConfig with JwtKey {

  // Get the play secret key defined in the app config file.
  // Have to be pass as an environment variable during production
  private val secretKey: String = config.getString("play.http.secret.key")

  def apply(headers: Headers): Either[ErrorMsg, UserDetail] = {
    // check if the header has 'Authorization' field or not
    headers.get("Authorization") match {
      case Some(token) => getUserDetails(token)
      case None        => Left(NoAuthorizationField)
    }
  }

  private def getUserDetails(token: String): Either[ErrorMsg, UserDetail] = {
    // Default Signature algorithm, defined in app config under: play.http.session.algorithm
    JwtJson.decodeJson(token, secretKey, Seq(JwtAlgorithm.HS256)) match {
      case Success(value)     => Right((value \ jwtSessionKey).as[UserDetail])
      case Failure(exception) => Left(TokenDecodeFailure(exception.getMessage))
    }
  }
}
