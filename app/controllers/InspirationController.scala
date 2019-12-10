package controllers

import daos.{CSVQuotesQueryDAO, CustomQuotesQueryDAO}
import javax.inject._
import models.{CSVQuotesQuery, CustomQuotesQuery}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an 'Action' to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class InspirationController @Inject()(
  cc: ControllerComponents,
  csvQuotesDAO: CSVQuotesQueryDAO,
  customerQuotesDAO: CustomQuotesQueryDAO
)(implicit executionContext: ExecutionContext) extends AbstractController(cc) {

  /**
   * The mapping for the QuotesQuery form.
   */
  val quotesQueryForm: Form[CustomQuotesQuery] = Form {
    mapping(
      "id" -> number,
      "quote" -> nonEmptyText.verifying(_.nonEmpty),
      "author" -> nonEmptyText,
      "genre" -> text,
      "storeddate" -> sqlDate,
      "ownquote" -> boolean
    )(CustomQuotesQuery.apply)(CustomQuotesQuery.unapply)
  }

  /**
   * A REST endpoint that gets a random quote as JSON.
   */
  def getRandomQuote: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    csvQuotesDAO.listAllQuotes().map { quoteQueries =>
      val randomQuotes: CSVQuotesQuery = quoteQueries(scala.util.Random.nextInt(quoteQueries.size))
      Ok(Json.toJson(randomQuotes))
    }
  }

  /**
   * A REST endpoint that gets first 10 quotes as JSON.
   */
  def getFirst10Quotes: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    csvQuotesDAO.listAllQuotes().map { quoteQueries =>
      val first100Quotes: Seq[CSVQuotesQuery] = quoteQueries.take(10)
      Ok(Json.toJson(first100Quotes))
    }
  }

  def getCustomQuotes: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    customerQuotesDAO.listCustomQuotes().map(quote => Ok(Json.toJson(quote)))
  }

  def addCustomQuote(): Action[AnyContent] = Action.async {implicit request: Request[AnyContent] =>
    quotesQueryForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(Ok("error" + formWithErrors))
        },
      customQuote => {
        customerQuotesDAO.createQuote(customQuote.copy(storedDate = new java.sql.Date(System.currentTimeMillis()))).map { _ =>
          Redirect(routes.InspirationController.getCustomQuotes())
        }
      }
    )
  }
}
