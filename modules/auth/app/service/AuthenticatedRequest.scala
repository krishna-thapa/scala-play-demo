package service

import model.UserDetail
import play.api.mvc._

case class AuthenticatedRequest[A](user: UserDetail, request: Request[A]) extends WrappedRequest[A](request)
