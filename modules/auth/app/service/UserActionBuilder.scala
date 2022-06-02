package service

import java.time.Clock
import com.krishna.response.ErrorMsg.AuthenticationFailed
import com.krishna.response.ResponseResult
import com.krishna.util.UtilImplicits.ErrorRecover
import config.JwtKey

import javax.inject.Inject
import model.UserDetail
import pdi.jwt.JwtSession._
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

class UserActionBuilder @Inject() (parser: BodyParsers.Default)(implicit
  ec: ExecutionContext,
  conf: Configuration
) extends ActionBuilderImpl(parser)
    with ResponseResult
    with JwtKey {

  implicit val clock: Clock = Clock.systemUTC

  override def invokeBlock[A](
    request: Request[A],
    block: Request[A] => Future[Result]
  ): Future[Result] = {
    log.info("Executing the authService service for UserActionBuilder")
    if (jwtSessionKey == "mockUser") block(request)
    else {
      request.jwtSession.getAs[UserDetail](jwtSessionKey) match {
        /*
        If want to make the admin role NOT to have permission that logged in user have
        Change to: case Some(userDetail) if !userDetail.isAdmin =>
         */
        case Some(userDetail) =>
          log.info(s"Giving access to the user: ${ userDetail.name }")
          block(new AuthenticatedRequest[A](userDetail, request))
            .map(_.refreshJwtSession(request))
            .errorRecover
        case _ =>
          responseErrorResult(AuthenticationFailed)
      }
    }
  }

}
