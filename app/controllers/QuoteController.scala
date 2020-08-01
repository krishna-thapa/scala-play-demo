package controllers

import daos.{ CSVQuotesQueryDAO, FavQuoteQueryDAO }
import javax.inject._
import models.CSVQuotesQuery
import models.Genre.Genre
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc._
import utils.Logging

import scala.concurrent.ExecutionContext

/**
  * This controller creates an 'Action' to handle HTTP requests to the
  * application's quotes from CSV Query table.
  */
@Singleton
class QuoteController @Inject()(
    cc: ControllerComponents,
    csvQuotesDAO: CSVQuotesQueryDAO,
    favQuotesDAO: FavQuoteQueryDAO
)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc)
    with Logging {

  /**
    * The mapping for the FavQuote form.
    */
  val favQuotesForm: Form[FavQuoteForm] = Form {
    mapping(
      "csvid"  -> nonEmptyText,
      "favtag" -> boolean
    )(FavQuoteForm.apply)(FavQuoteForm.unapply)
  }

  /**
    * A REST endpoint that gets a random quote as JSON from CSV quotes table.
    */
  def getRandomQuote: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val allQuotes: Seq[CSVQuotesQuery] = csvQuotesDAO.listAllQuotes()
    Ok(Json.toJson(allQuotes))
  }

  //def getRandomQuote

  /**
    * A REST endpoint that gets first 10 quotes as JSON.
    */
  def getFirst10Quotes: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(Json.toJson(csvQuotesDAO.listAllQuotes().take(10)))
  }

  /**
    * A REST endpoint that creates or altered the fav tag in the fav_quotes table.
    */
  def favQuote(csvid: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      favQuotesDAO.modifyFavquote(csvid).map { quote =>
        Ok(Json.toJson(quote))
      }
  }

  /**
    * A REST endpoint that gets all favorite quotes as JSON.
    */
  def getFavQuotes: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(Json.toJson(csvQuotesDAO.listAllFavQuotes()))
  }

  /**
    * A REST endpoint that gets a random quote as per selected genre from the table.
    */
  def getGenreQuote(genre: Genre): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      csvQuotesDAO.getGenreQuote(genre) match {
        case Some(quote) => Ok(Json.toJson(quote))
        case None =>
          log.warn(s"Database is empty with that genre: $genre")
          NotFound("Database is empty with that genre!")
      }
  }

  case class FavQuoteForm(csvid: String, favTag: Boolean)
}
