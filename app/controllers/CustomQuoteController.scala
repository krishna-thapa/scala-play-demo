package controllers

//import controllers.custom.routes
import daos.CustomQuoteQueryDAO
import forms.RequestForm
import helper.ResponseMethod
import javax.inject.{ Inject, Singleton }
import models.CustomQuotesQuery
import play.api.libs.json.Json
import play.api.mvc._
import utils.Logging

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CustomQuoteController @Inject()(
    cc: ControllerComponents,
    customerQuotesDAO: CustomQuoteQueryDAO
)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc)
    with ResponseMethod
    with Logging {

  /**
    * A REST endpoint that gets all the custom quotes.
    */
  def getCustomQuotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getCustomQuotes")
    responseSeqResult(customerQuotesDAO.listCustomQuotes())
  }

  /**
    * A REST endpoint that gets a random quote as JSON from Custom quotes table.
    */
  def getRandomCustomQuote: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getRandomCustomQuote")
    responseOptionResult(customerQuotesDAO.listRandomQuote())
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
      customerQuotesDAO.deleteQuote(id)
      log.warn(s"Successfully delete entry $id")
      Ok(s"Successfully delete entry $id")
  }

  /**
    * A REST endpoint that add a new quote as JSON to Custom quotes table.
    */
  def addCustomQuote(): Action[AnyContent] = Action { implicit request =>
    Ok("Your new application is ready.")
  /*    RequestForm.quotesQueryForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest("error" + formWithErrors))
      },
      customQuote => {
        customerQuotesDAO.createQuote(customQuote).map { _ =>
          Redirect(routes.CustomQueryController.getCustomQuotes())
        }
      }
    )*/
  }

  /**
    * A REST endpoint that updated selected quote to Custom quotes table.
    */
  def updateCustomQuote(id: Int): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok("Your new application is ready.")
    /*      RequestForm.quotesQueryForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest("error" + formWithErrors))
        },
        customQuote => {
          customerQuotesDAO.updateQuote(id, customQuote).map { _ =>
            Redirect(routes.CustomQueryController.getCustomQuotes())
          }
        }
      )*/
  }

}
