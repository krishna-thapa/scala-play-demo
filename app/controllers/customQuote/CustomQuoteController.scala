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

  /**
    * A REST endpoint that gets all the custom quotes for the logged in user.
    */
  def getCustomQuotes: Action[AnyContent] = UserAction { implicit request =>
    val user: UserDetail = DecodeHeader(request.headers)
    log.info(s"Executing getCustomQuotes by user: ${user.email}")
    responseSeqResult(customerQuotesDAO.listAllQuotes(user.id))
  }

  /**
    * A REST endpoint that gets a random quote as JSON from Custom quotes table.
    */
  def getRandomCustomQuote: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getRandomCustomQuote")
    responseSeqResult(customerQuotesDAO.listRandomQuote(1))
  }

  /**
    * A REST endpoint that gets a selected quote as JSON from Custom quotes table.
    */
  def getSelectedQuote(id: Int): Action[AnyContent] = Action { implicit request =>
    log.info("Executing getSelectedQuote")
    responseOptionResult(customerQuotesDAO.listSelectedQuote(id))
  }

  /**
    * A REST endpoint that deletes selected quote as JSON from Custom quotes table.
    */
  def deleteCustomQuote(id: Int): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      if (customerQuotesDAO.deleteQuote(id) > 0) {
        log.warn(s"Successfully delete entry $id")
        responseOk(OkResponse(s"Successfully delete entry $id"))
      } else {
        val error = s"Error on request with id: $id"
        badRequest(error)
      }
  }

  /**
    * A REST endpoint that add a new quote as JSON to Custom quotes table.
    */
  def addCustomQuote(): Action[AnyContent] = Action { implicit request =>
    log.info("Executing addCustomQuote")
    // Add request validation
    RequestForm.quotesQueryForm.bindFromRequest.fold(
      formWithErrors => {
        badRequest("error" + formWithErrors)
      },
      customQuote => {
        responseOk(customerQuotesDAO.createQuote(customQuote))
      }
    )
  }

  /**
    * A REST endpoint that updated selected quote to Custom quotes table.
    * TODO: NOT WORKING
    */
  def updateCustomQuote(id: Int): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      log.info("Executing updateCustomQuote")
      RequestForm.quotesQueryForm.bindFromRequest.fold(
        formWithErrors => {
          badRequest("error" + formWithErrors)
        },
        customQuote => {
          customerQuotesDAO.updateQuote(id, customQuote) match {
            case Success(id)        => responseOk(OkResponse(s"Successfully updated entry row $id"))
            case Failure(exception) => internalServerError(exception.getMessage)
          }
        }
      )
  }

}
