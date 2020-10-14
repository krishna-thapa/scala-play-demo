package controllers.authController

import java.time.Clock

import auth.form.LoginForm
import auth.model.UserDetail
import javax.inject.{Inject, Singleton}
import pdi.jwt.JwtSession._
import play.api.Configuration
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, Request }
import response.ResponseMethod
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject()(cc: ControllerComponents)
  (implicit executionContext: ExecutionContext, config: Configuration)
    extends AbstractController(cc)
    with Logging
    with ResponseMethod
{

  private val passwords: Seq[String] = Seq("foo", "poo")

  implicit val clock: Clock = Clock.systemUTC

  def login: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>

    val loginResult = LoginForm.loginForm.bindFromRequest.fold(
      formWithErrors => {
        badRequest("The form was not in the expected format: " + formWithErrors)
      },
      loginDetails => {
        if(passwords.contains(loginDetails.password)) {
          log.info("Success on authentication!")
          Ok.addingToJwtSession("user", UserDetail(loginDetails.username))
        } else
          unauthorized(s"Unauthorized error for user: ${loginDetails.username}")
      }
    )
    Future(loginResult)
  }

}
