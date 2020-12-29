package controllers.customQuote

import com.krishna.response.{ OkResponse, ResponseResult }
import com.krishna.util.Logging
import dao.CustomQuoteQueryDAO
import depInject.{ SecuredController, SecuredControllerComponents }
import forms.RequestForm
import model.UserDetail

import javax.inject.{ Inject, Singleton }
import play.api.mvc._
import util.DecodeHeader

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

@Singleton
class CustomQuoteController @Inject()(
    ss: SecuredControllerComponents,
    customerQuotesDAO: CustomQuoteQueryDAO
)(implicit executionContext: ExecutionContext)
    extends SecuredController(ss)
    with ResponseResult
    with Logging {

  private lazy val getLoggedUser: Request[AnyContent] => UserDetail =
    (request: Request[AnyContent]) => DecodeHeader(request.headers)

  /**
    * A REST endpoint that gets all the custom quotes for the logged in user.
    */
  def getCustomQuotes: Action[AnyContent] = UserAction { implicit request =>
    val user: UserDetail = getLoggedUser(request)
    log.info(s"Executing getCustomQuotes by user: ${user.email}")
    responseSeqResult(customerQuotesDAO.listAllQuotes(user.id))
  }

  /**
    * A REST endpoint that gets a random quote as JSON from Custom quotes table.
    */
  def getRandomCustomQuote: Action[AnyContent] = UserAction { implicit request =>
    val user: UserDetail = getLoggedUser(request)
    log.info(s"Executing getRandomCustomQuote by user: ${user.email}")
    responseSeqResult(customerQuotesDAO.listRandomQuote(1, user.id))
  }

  /**
    * A REST endpoint that gets a selected quote as JSON from Custom quotes table.
    */
  def getSelectedQuote(id: Int): Action[AnyContent] = UserAction { implicit request =>
    val user: UserDetail = getLoggedUser(request)
    log.info(s"Executing getSelectedQuote by user: ${user.email}")
    responseOptionResult(customerQuotesDAO.listSelectedQuote(id, user.id))
  }

  /**
    * A REST endpoint that deletes selected quote as JSON from Custom quotes table.
    */
  def deleteCustomQuote(id: Int): Action[AnyContent] = UserAction {
    implicit request: Request[AnyContent] =>
      val user: UserDetail = getLoggedUser(request)
      log.info(s"Executing deleteCustomQuote by user: ${user.email}")

      if (customerQuotesDAO.deleteQuote(id, user.id) > 0) {
        log.warn(s"Successfully delete entry $id")
        responseOk(OkResponse(s"Successfully delete entry $id"))
      } else {
        badRequest(s"Error on request with id: $id")
      }
  }

  /**
    * A REST endpoint that add a new quote as JSON to Custom quotes table.
    * It takes the userinfo to add userid and user name in custom quotes tagble
    */
  def addCustomQuote(): Action[AnyContent] = UserAction { implicit request =>
    val user: UserDetail = getLoggedUser(request)
    log.info(s"Executing addCustomQuote by user: ${user.email}")
    RequestForm.quotesQueryForm.bindFromRequest.fold(
      formWithErrors => {
        badRequest("error" + formWithErrors.errors)
      },
      customQuote => {
        responseOk(customerQuotesDAO.createQuote(customQuote, user))
      }
    )
  }

  /**
    * A REST endpoint that updated selected quote to Custom quotes table.
    * TODO: NOT WORKING
    */
  def updateCustomQuote(id: Int): Action[AnyContent] = UserAction {
    implicit request: Request[AnyContent] =>
      val user: UserDetail = getLoggedUser(request)
      log.info(s"Executing updateCustomQuote by user: ${user.email}")
      RequestForm.quotesQueryForm.bindFromRequest.fold(
        formWithErrors => {
          badRequest("error" + formWithErrors.errors)
        },
        customQuote => {
          customerQuotesDAO.updateQuote(id, user.id, customQuote) match {
            case Success(id)        => responseOk(OkResponse(s"Successfully updated entry row $id"))
            case Failure(exception) => internalServerError(exception.getMessage)
          }
        }
      )
  }

}
