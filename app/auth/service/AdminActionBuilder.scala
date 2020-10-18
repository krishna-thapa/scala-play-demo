package auth.service

import java.time.Clock

import auth.model.UserToken
import javax.inject.Inject
import pdi.jwt.JwtSession.RichRequestHeader
import play.api.mvc._
import response.ResponseError
import pdi.jwt.JwtSession._
import play.api.Configuration

import scala.concurrent.{ ExecutionContext, Future }

class AdminActionBuilder @Inject()(parser: BodyParsers.Default)(
    implicit ec: ExecutionContext,
    conf: Configuration
) extends ActionBuilderImpl(parser)
    with ResponseError {

  implicit val clock: Clock = Clock.systemUTC

  override def invokeBlock[A](
      request: Request[A],
      block: Request[A] => Future[Result]
  ): Future[Result] = {
    log.info("Executing the auth service for AdminActionBuilder")
    request.jwtSession.getAs[UserToken]("user") match {
      case Some(userToken) if userToken.isAdmin =>
        block(new AuthenticatedRequest[A](userToken, request)).map(_.refreshJwtSession(request))
      // If the logged in user doesn't have an admin role
      case Some(userToken) =>
        Future(forbidden(s"${userToken.email}").refreshJwtSession(request))
      case _ =>
        Future(unauthorized(s"Do not have access!"))
    }
  }
}
