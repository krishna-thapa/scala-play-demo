package controllers

import daos.QuotesQueryDAO
import javax.inject._
import models.QuotesQuery
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
  quotesQueryDAO: QuotesQueryDAO
)(implicit executionContext: ExecutionContext) extends AbstractController(cc) {

  /**
   * The mapping for the QuotesQuery form.
   */
  val quotesQueryForm: Form[QuotesQuery] = Form {
    mapping(
      "id" -> number,
      "quote" -> nonEmptyText.verifying(_.nonEmpty),
      "author" -> nonEmptyText,
      "genre" -> text
    )(QuotesQuery.apply)(QuotesQuery.unapply)
  }

  /**
   * A REST endpoint that gets a random quote as JSON.
   */
  def getQuote: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    quotesQueryDAO.list().map { quoteQueries =>
      val randomQuotes: QuotesQuery = quoteQueries(scala.util.Random.nextInt(quoteQueries.size))
      Ok(Json.toJson(randomQuotes))
    }
  }

  /**
   * A REST endpoint that gets first 100 quotes as JSON.
   */
  def getQuotes: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    quotesQueryDAO.list().map { quoteQueries =>
      val first100Quotes: Seq[QuotesQuery] = quoteQueries.take(100)
      Ok(Json.toJson(first100Quotes))
    }
  }
}
