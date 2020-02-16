package controllers.custom

import daos.CustomQuotesQueryDAO
import javax.inject.{Inject, Singleton}
import models.Genre.Genre
import models.CustomQuoteForm
import org.slf4j.LoggerFactory
import play.api.data.{Form, Forms}
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomQueryController @Inject()(
    cc: ControllerComponents,
    customerQuotesDAO: CustomQuotesQueryDAO
)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc) {

  private val logger = LoggerFactory.getLogger(classOf[CustomQueryController])

  /**
    * The mapping for the QuotesQuery form.
    */
  val quotesQueryForm: Form[CustomQuoteForm] = Form {
    mapping(
      "quote"    -> nonEmptyText.verifying(_.nonEmpty),
      "author"   -> nonEmptyText,
      "genre"    -> Forms.of[Genre],
      "ownquote" -> boolean
    )(CustomQuoteForm.apply)(CustomQuoteForm.unapply)
  }

  /**
    * A REST endpoint that gets all the custom quotes.
    */
  def getCustomQuotes: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    customerQuotesDAO.listCustomQuotes().map(quote => Ok(Json.toJson(quote)))
  }

  /**
    * A REST endpoint that gets a random quote as JSON from Custom quotes table.
    */
  def getRandomCustomQuote: Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      customerQuotesDAO.listRandomQuote().map {
        case Some(quote) => Ok(Json.toJson(quote))
        case None =>
          logger.warn("Database is empty")
          NotFound("Database is empty!")
      }
  }

  /**
    * A REST endpoint that gets a selected quote as JSON from Custom quotes table.
    */
  def getSelectedQuote(id: Int): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      customerQuotesDAO.listSelectedQuote(id).map {
        case Some(quote) => Ok(Json.toJson(quote))
        case None =>
          logger.warn(s"Record with id:$id, not found in Database!")
          NotFound(s"Record with id:$id, not found in Database!")
      }
  }

  /**
    * A REST endpoint that add a new quote as JSON to Custom quotes table.
    */
  def addCustomQuote(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
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

  /**
    * A REST endpoint that updated selected quote to Custom quotes table.
    */
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

  /**
    * A REST endpoint that deletes selected quote as JSON from Custom quotes table.
    */
  def deleteCustomQuote(id: Int): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      customerQuotesDAO.deleteQuote(id)
      logger.warn(s"Successfully delete entry $id")
      Ok(s"Successfully delete entry $id")
  }
}
