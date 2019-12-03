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
      "index" -> number,
      "author" -> nonEmptyText,
      "quote" -> nonEmptyText.verifying(_.nonEmpty)
    )(QuotesQuery.apply)(QuotesQuery.unapply)
  }

  def index(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    quotesQueryDAO.list().map { quoteQueries =>
      Ok(quoteQueries.map(_.author))
    }
    //Ok(generateQuote(scala.util.Random.nextInt(10)))
  }

  /*def generateQuote(random: Int): String = {
    val x: Future[Seq[String]] = quotesQueryDAO.list().map { quote =>
      quote.map {
        q => q.author
      }
    }
  }*/

}
