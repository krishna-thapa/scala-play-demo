package config

import com.krishna.response.ErrorMsg
import com.krishna.response.ErrorMsg.{ NoAuthorizationField, TokenDecodeFailure }
import model.UserDetail
import pdi.jwt.{ JwtAlgorithm, JwtJson }
import play.api.Configuration
import play.api.mvc.Headers

import scala.util.{ Failure, Success }

case class DecodeHeader()(implicit conf: Configuration) extends JwtKey {

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
