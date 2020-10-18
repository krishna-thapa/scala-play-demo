package auth.service

import auth.model.UserToken
import play.api.mvc._

case class AuthenticatedRequest[A](user: UserToken, request: Request[A])
    extends WrappedRequest[A](request)
