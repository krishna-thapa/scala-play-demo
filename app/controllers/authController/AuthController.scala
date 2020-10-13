package controllers.authController

import java.time.Clock

import auth.model.UserDetail
import javax.inject.{Inject, Singleton}
import pdi.jwt.JwtSession._
import play.api.Configuration
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, JsValue, Reads}
import play.api.mvc.{AbstractController, Action, ControllerComponents, Request}
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

  private val loginForm: Reads[(String, String)] =
    ((JsPath \ "username"). read[String] and (JsPath \ "password").read[String]).tupled

  implicit val clock: Clock = Clock.systemUTC

  def login: Action[JsValue] = Action(parse.json).async { implicit request: Request[JsValue] =>
    val loginResult = request.body
      .validate(loginForm)
      .fold(
        errors => {
          log.error(s"Error on authentication: $errors")
          badRequest(errors.toString())
        }, {
          case (username, password) =>
            if(passwords.contains(password)) {
              log.info("Success on authentication!")
              Ok.addingToJwtSession("user", UserDetail(username))
            } else
              Unauthorized
        }
      )
    Future(loginResult)
  }

}
