package service

import java.time.Clock
import com.krishna.response.ErrorMsg.{ AuthenticationFailed, AuthorizationForbidden }
import com.krishna.response.ResponseResult
import com.krishna.util.FutureErrorHandler.ErrorRecover

import javax.inject.Inject
import model.UserDetail
import pdi.jwt.JwtSession.RichRequestHeader
import play.api.mvc._
import pdi.jwt.JwtSession._
import play.api.Configuration
import config.JwtKey

import scala.concurrent.{ ExecutionContext, Future }

class AdminActionBuilder @Inject()(parser: BodyParsers.Default)(
    implicit ec: ExecutionContext,
    conf: Configuration
) extends ActionBuilderImpl(parser)
    with ResponseResult
    with JwtKey {

  implicit val clock: Clock = Clock.systemUTC

  override def invokeBlock[A](
      request: Request[A],
      block: Request[A] => Future[Result]
  ): Future[Result] = {
    log.info("Executing the authService service for AdminActionBuilder")
    request.jwtSession.getAs[UserDetail](jwtSessionKey) match {
      case Some(userDetail) if userDetail.isAdmin =>
        block(new AuthenticatedRequest[A](userDetail, request))
          .map(_.refreshJwtSession(request))
          .errorRecover
      // If the logged in user doesn't have an admin role
      case Some(userDetail) =>
        Future(
          responseErrorResult(AuthorizationForbidden(s"${userDetail.email}"))
            .refreshJwtSession(request)
        )
      case _ =>
        Future(responseErrorResult(AuthenticationFailed))
    }
  }
}
