package auth.depInject

import auth.service.UserActionBuilder
import javax.inject.Inject
import play.api.http.FileMimeTypes
import play.api.i18n.{ Langs, MessagesApi }
import play.api.mvc._

case class SecuredControllerComponents @Inject()(
    authenticatedActionBuilder: UserActionBuilder,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: scala.concurrent.ExecutionContext
) extends ControllerComponents
