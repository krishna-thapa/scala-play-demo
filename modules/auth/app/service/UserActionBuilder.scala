package service

import java.time.Clock

import com.krishna.response.ResponseError
import javax.inject.Inject
import model.UserToken
import pdi.jwt.JwtSession._
import play.api.Configuration
import play.api.mvc._
import util.JwtKey

import scala.concurrent.{ ExecutionContext, Future }

class UserActionBuilder @Inject()(parser: BodyParsers.Default)(
    implicit ec: ExecutionContext,
    conf: Configuration
) extends ActionBuilderImpl(parser)
    with ResponseError
    with JwtKey {

  implicit val clock: Clock = Clock.systemUTC

  override def invokeBlock[A](
      request: Request[A],
      block: Request[A] => Future[Result]
  ): Future[Result] = {
    log.info("Executing the authService service for UserActionBuilder")
    request.jwtSession.getAs[UserToken](jwtSessionKey) match {
      /*
      If want to make the admin role NOT to have permission that logged in user have
      Change to: case Some(userToken) if !userToken.isAdmin =>
       */
      case Some(userToken) =>
        log.info(s"Giving access to the user: ${userToken.fullName}")
        block(new AuthenticatedRequest[A](userToken, request)).map(_.refreshJwtSession(request))
      case _ =>
        Future(unauthorized(s"Do not have access!"))
    }
  }
}
