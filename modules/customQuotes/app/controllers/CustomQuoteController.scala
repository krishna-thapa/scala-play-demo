package controllers.customQuotes

import com.krishna.response.ErrorMsg.RecordNotFound
import com.krishna.response.{ OkResponse, ResponseResult }
import dao.CustomQuoteQueryDAO
import depInject.{ SecuredController, SecuredControllerComponents }
import forms.RequestForm
import model.UserDetail
import javax.inject.{ Inject, Singleton }
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

@Singleton
class CustomQuoteController @Inject() (
  ss: SecuredControllerComponents,
  customerQuotesDAO: CustomQuoteQueryDAO
)(implicit executionContext: ExecutionContext)
    extends SecuredController(ss)
    with ResponseResult {

  /**
    * A REST endpoint that gets all the custom quotes for the logged in user.
    */
  def getCustomQuotes: Action[AnyContent] = UserAction { implicit request =>
    log.info(s"Executing getCustomQuotes from Custom quotes")
    val customQuotes =
      (user: UserDetail) => responseSeqResult(customerQuotesDAO.listAllQuotes(user.id))
    getResultForCustomQuote(request, customQuotes)
  }

  /**
    * A REST endpoint that gets a random quote as JSON from Custom quotes table.
    */
  def getRandomCustomQuote: Action[AnyContent] = UserAction { implicit request =>
    log.info(s"Executing getRandomCustomQuote from Custom quotes")
    val randomCustomQuote =
      (user: UserDetail) => responseSeqResult(customerQuotesDAO.listRandomQuote(1, user.id))
    getResultForCustomQuote(request, randomCustomQuote)
  }

  /**
    * A REST endpoint that gets a selected quote as JSON from Custom quotes table.
    */
  def getSelectedQuote(id: Int): Action[AnyContent] = UserAction { implicit request =>
    log.info(s"Executing getSelectedQuote from Custom quotes")
    val selectedQuote =
      (user: UserDetail) => responseOptionResult(customerQuotesDAO.listSelectedQuote(id, user.id))
    getResultForCustomQuote(request, selectedQuote)
  }

  /**
    * A REST endpoint that deletes selected quote as JSON from Custom quotes table.
    */
  def deleteCustomQuote(id: Int): Action[AnyContent] = UserAction {
    implicit request: Request[AnyContent] =>
      log.info(s"Executing deleteCustomQuote from Custom quotes")

      val deletedQuote = (user: UserDetail) => {
        if (customerQuotesDAO.deleteQuote(id, user.id) > 0) {
          log.warn(s"Successfully deleted custom quote with id: $id")
          responseOk(OkResponse(s"Successfully delete quote with id: $id"))
        } else {
          badRequest(s"Error on request with quote id: $id")
        }
      }

      getResultForCustomQuote(request, deletedQuote)
  }

  /**
    * A REST endpoint that add a new quote as JSON to Custom quotes table.
    * It takes the userinfo to add userid and user name in custom quotes tagble
    */
  def addCustomQuote(): Action[AnyContent] = UserAction { implicit request =>
    log.info(s"Executing addCustomQuote from Custom quotes")
    log.warn(
      s"Executing addCustomQuote from Custom quotes ${ RequestForm.quotesQueryForm.bindFromRequest() }"
    )
    val addQuote = (user: UserDetail) => {
      RequestForm
        .quotesQueryForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            badRequest("error" + formWithErrors.errors)
          },
          customQuote => {
            responseOk(customerQuotesDAO.createQuote(customQuote, user))
          }
        )
    }

    getResultForCustomQuote(request, addQuote)
  }

  /**
    * A REST endpoint that updated selected quote to Custom quotes table.
    */
  def updateCustomQuote(id: Int): Action[AnyContent] = UserAction {
    implicit request: Request[AnyContent] =>
      log.info(s"Executing updateCustomQuote from Custom quotes")

      val updateQuote = (user: UserDetail) => {
        RequestForm
          .quotesQueryForm
          .bindFromRequest()
          .fold(
            formWithErrors => {
              badRequest("error" + formWithErrors.errors)
            },
            customQuote => {
              customerQuotesDAO.updateQuote(id, user.id, customQuote) match {
                case Success(recordsUpdated) if recordsUpdated == 1 =>
                  responseOk(OkResponse(s"Successfully updated record with id: $id"))
                case Success(_) =>
                  notFound(RecordNotFound(id))
                case Failure(exception) => internalServerError(exception.getMessage)
              }
            }
          )
      }

      getResultForCustomQuote(request, updateQuote)
  }

  private def getResultForCustomQuote(
    request: Request[AnyContent],
    result: UserDetail => Result
  ): Result = {
    customerQuotesDAO.decoderHeader(request) match {
      case Left(errorMsg) => responseErrorResult(errorMsg)
      case Right(user) =>
        log.info(s"Executing CustomQuoteController for user: ${ user.email }")
        result(user)
    }
  }

}
