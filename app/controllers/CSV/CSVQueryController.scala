package controllers.CSV

import daos.{CSVQuotesQueryDAO, CustomQuotesQueryDAO}
import javax.inject._
import models.CSVQuotesQuery
import models.Genre.Genre
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * This controller creates an 'Action' to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class CSVQueryController @Inject()(
    cc: ControllerComponents,
    csvQuotesDAO: CSVQuotesQueryDAO,
    customerQuotesDAO: CustomQuotesQueryDAO
)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc) {

  /**
    * A REST endpoint that gets a random quote as JSON.
    */
  def getRandomQuote: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    csvQuotesDAO.listAllQuotes().map { quoteQueries =>
      val randomQuotes: CSVQuotesQuery =
        quoteQueries(scala.util.Random.nextInt(quoteQueries.size))
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

  def getGenreQuote(genre: Genre): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      csvQuotesDAO.getGenreQuote(genre).map {
        case Some(quote) => Ok(Json.toJson(quote))
        case None        => NotFound(s"Database is empty with that genre!")
      }
  }
}
