package controllers.custom

import daos.CustomQuotesQueryDAO
import javax.inject.{Inject, Singleton}
import models.Genre.Genre
import models.{CustomQuoteForm, Genre}
import play.api.data.{Form, Forms}
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomQueryController @Inject() (
    cc: ControllerComponents,
    customerQuotesDAO: CustomQuotesQueryDAO
)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc) {

  /**
    * The mapping for the QuotesQuery form.
    */
  val quotesQueryForm: Form[CustomQuoteForm] = Form {
    mapping(
      "quote" -> nonEmptyText.verifying(_.nonEmpty),
      "author" -> nonEmptyText,
      "genre" -> Forms.of[Genre],
      "ownquote" -> boolean
    )(CustomQuoteForm.apply)(CustomQuoteForm.unapply)
  }

  def getCustomQuotes: Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      customerQuotesDAO.listCustomQuotes().map(quote => Ok(Json.toJson(quote)))
  }

  def getRandomCustomQuote: Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      customerQuotesDAO.listRandomQuote().map {
        case Some(quote) => Ok(Json.toJson(quote))
        case None        => NotFound(s"Database is empty!")
      }
  }

  def getSelectedQuote(id: Int): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      customerQuotesDAO.listSelectedQuote(id).map {
        case Some(quote) => Ok(Json.toJson(quote))
        case None        => NotFound(s"Record with id:$id, not found in Database!")
      }
  }

  def addCustomQuote(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      quotesQueryForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest("error" + formWithErrors))
        },
        customQuote => {
          customerQuotesDAO.createQuote(customQuote).map { _ =>
            Redirect(routes.CustomQueryController.getCustomQuotes())
          }
        }
      )
  }

  def updateCustomQuote(id: Int): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      quotesQueryForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest("error" + formWithErrors))
        },
        customQuote => {
          customerQuotesDAO.updateQuote(id, customQuote).map { _ =>
            Redirect(routes.CustomQueryController.getCustomQuotes())
          }
        }
      )
  }

  def deleteCustomQuote(id: Int): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      customerQuotesDAO.deleteQuote(id)
      Ok(s"Successfully delete entry $id")
  }
}
