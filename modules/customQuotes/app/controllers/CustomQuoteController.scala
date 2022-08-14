package controllers.customQuotes

import com.krishna.response.ErrorMsg.RecordNotFound
import com.krishna.response.{ OkResponse, ResponseResult }
import com.krishna.util.UtilImplicits.ErrorRecover
import dao.CustomQuoteQueryDAO
import depInject.{ SecuredController, SecuredControllerComponents }
import forms.RequestForm
import model.UserDetail
import play.api.mvc._

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

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
  def getCustomQuotes: Action[AnyContent] = UserAction.async { implicit request =>
    log.info(s"Executing getCustomQuotes in CustomQuoteController.")
    val customQuotes =
      (user: UserDetail) => responseSeqResultAsync(customerQuotesDAO.listAllQuotes(user.userId))
    getResultForCustomQuote(request, customQuotes)
  }

  /**
    * A REST endpoint that gets a random quote as JSON from Custom quotes table.
    */
  def getRandomCustomQuote: Action[AnyContent] = UserAction.async { implicit request =>
    log.info(s"Executing getRandomCustomQuote in CustomQuoteController.")
    val randomCustomQuote =
      (user: UserDetail) =>
        responseSeqResultAsync(customerQuotesDAO.listRandomQuote(1, user.userId))
    getResultForCustomQuote(request, randomCustomQuote)
  }

  /**
    * A REST endpoint that gets a selected quote as JSON from Custom quotes table.
    */
  def getSelectedQuote(id: Int): Action[AnyContent] = UserAction.async { implicit request =>
    log.info(s"Executing getSelectedQuote in CustomQuoteController.")
    val selectedQuote =
      (user: UserDetail) =>
        responseOptionResult(customerQuotesDAO.listSelectedQuote(id, user.userId))
    getResultForCustomQuote(request, selectedQuote)
  }

  /**
    * A REST endpoint that deletes selected quote as JSON from Custom quotes table.
    */
  def deleteCustomQuote(id: Int): Action[AnyContent] = UserAction.async {
    implicit request: Request[AnyContent] =>
      log.info(s"Executing deleteCustomQuote in CustomQuoteController.")

      val deletedQuote = (user: UserDetail) => {
        customerQuotesDAO.deleteQuote(id, user.userId).flatMap { response =>
          if (response > 0) {
            log.warn(s"Successfully deleted custom quote with id: $id")
            responseOk(OkResponse(s"Successfully delete quote with id: $id"))
          } else {
            badRequest(s"Error on request with quote id: $id")
          }
        }
      }

      getResultForCustomQuote(request, deletedQuote)
  }

  /**
    * A REST endpoint that add a new quote as JSON to Custom quotes table.
    * It takes the userinfo to add userid and user name in custom quotes tagble
    */
  def addCustomQuote(): Action[AnyContent] = UserAction.async { implicit request =>
    log.info(s"Executing addCustomQuote in CustomQuoteController.")

    val addQuote = (user: UserDetail) => {
      RequestForm
        .quotesQueryForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            badRequest("error" + formWithErrors.errors)
          },
          customQuote => {
            responseOkAsync(customerQuotesDAO.createQuote(customQuote, user))
          }
        )
    }

    getResultForCustomQuote(request, addQuote)
  }

  /**
    * A REST endpoint that updated selected quote to Custom quotes table.
    */
  def updateCustomQuote(id: Int): Action[AnyContent] = UserAction.async {
    implicit request: Request[AnyContent] =>
      log.info(s"Executing updateCustomQuote in CustomQuoteController.")

      val updateQuote = (user: UserDetail) => {
        RequestForm
          .quotesQueryForm
          .bindFromRequest()
          .fold(
            formWithErrors => {
              badRequest("error" + formWithErrors.errors)
            },
            customQuote => {
              customerQuotesDAO.updateQuote(id, user.userId, customQuote).flatMap {
                recordsUpdated =>
                  if (recordsUpdated == 1)
                    responseOk(OkResponse(s"Successfully updated record with id: $id"))
                  else
                    notFound(RecordNotFound(id))
              }
            }
          )
      }

      getResultForCustomQuote(request, updateQuote)
  }

  private def getResultForCustomQuote(
    request: Request[AnyContent],
    customRunResult: UserDetail => Future[Result]
  ): Future[Result] = {
    customerQuotesDAO.decoderHeader(request) match {
      case Left(errorMsg) => responseErrorResult(errorMsg)
      case Right(user) =>
        log.info(s"Executing CustomQuoteController for user: ${ user.email }")
        customRunResult(user).errorRecover
    }
  }

}
