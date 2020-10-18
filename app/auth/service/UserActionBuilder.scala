package auth.service

import java.time.Clock

import auth.model.UserToken
import javax.inject.Inject
import pdi.jwt.JwtSession._
import play.api.Configuration
import play.api.mvc._
import response.ResponseError

import scala.concurrent.{ ExecutionContext, Future }

class UserActionBuilder @Inject()(parser: BodyParsers.Default)(
    implicit ec: ExecutionContext,
    conf: Configuration
) extends ActionBuilderImpl(parser)
    with ResponseError {

  implicit val clock: Clock = Clock.systemUTC

  override def invokeBlock[A](
      request: Request[A],
      block: Request[A] => Future[Result]
  ): Future[Result] = {
    log.info("Executing the auth service for UserActionBuilder")
    request.jwtSession.getAs[UserToken]("user") match {
      /*
      If want to make the admin role NOT to have permission that logged in user have
      Change to: case Some(userToken) if !userToken.isAdmin =>
       */
      case Some(userToken) =>
        block(new AuthenticatedRequest[A](userToken, request)).map(_.refreshJwtSession(request))
      case _ =>
        Future(unauthorized(s"Do not have access!"))
    }
  }
}
