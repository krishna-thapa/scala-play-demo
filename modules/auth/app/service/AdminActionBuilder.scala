package service

import java.time.Clock

import com.krishna.response.ResponseError
import javax.inject.Inject
import model.UserDetail
import pdi.jwt.JwtSession.RichRequestHeader
import play.api.mvc._
import pdi.jwt.JwtSession._
import play.api.Configuration
import util.JwtKey

import scala.concurrent.{ ExecutionContext, Future }

class AdminActionBuilder @Inject()(parser: BodyParsers.Default)(
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
    log.info("Executing the authService service for AdminActionBuilder")
    request.jwtSession.getAs[UserDetail](jwtSessionKey) match {
      case Some(userDetail) if userDetail.isAdmin =>
        block(new AuthenticatedRequest[A](userDetail, request)).map(_.refreshJwtSession(request))
      // If the logged in user doesn't have an admin role
      case Some(userDetail) =>
        Future(forbidden(s"${userDetail.email}").refreshJwtSession(request))
      case _ =>
        Future(unauthorized(s"Do not have access!"))
    }
  }
}
