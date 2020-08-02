package controllers

import daos.{ QuoteQueryDAO, FavQuoteQueryDAO }
import javax.inject._
import models.QuotesQuery
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
    csvQuotesDAO: QuoteQueryDAO,
    favQuotesDAO: FavQuoteQueryDAO
)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc)
    with Responses
    with Logging {

  /**
    * The mapping for the FavQuote form.
    */
//  val favQuotesForm: Form[FavQuoteForm] = Form {
//    mapping(
//      "csvid"  -> nonEmptyText,
//      "favtag" -> boolean
//    )(FavQuoteForm.apply)(FavQuoteForm.unapply)
//  }

  /**
    * A REST endpoint that gets a random quote as JSON from CSV quotes table.
    */
  def getRandomQuote: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getRandomQuote")
    responseResult(csvQuotesDAO.randomQuote(1))
  }

  /**
    * A REST endpoint that gets all the quotes as JSON from CSV quotes table.
    */
  def getAllQuotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getAllQuotes")
    responseResult(csvQuotesDAO.allQuotes())
  }

  /**
    * A REST endpoint that gets random 10 quotes as JSON from CSV quotes table.
    */
  def getFirst10Quotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getFirst10Quotes")
    responseResult(csvQuotesDAO.randomQuote(10))
  }

  /**
    * A REST endpoint that creates or altered the fav tag in the fav_quotes table.
    */
  def favQuote(csvid: String): Action[AnyContent] = Action.async {
    log.info("Executing favQuote")
    implicit request: Request[AnyContent] =>
      favQuotesDAO.modifyFavquote(csvid).map { quote =>
        Ok(Json.toJson(quote))
      }
  }

  /**
    * A REST endpoint that gets all favorite quotes as JSON.
    */
  def getFavQuotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getFavQuotes")
    responseResult(csvQuotesDAO.listAllFavQuotes())
  }

  /**
    * A REST endpoint that gets a random quote as per selected genre from the table.
    */
  def getGenreQuote(genre: Genre): Action[AnyContent] = Action { implicit request =>
    log.info("Executing getGenreQuote")
    Ok(Json.toJson(csvQuotesDAO.getGenreQuote(genre)))
  }

  def responseResult(quotes: Seq[QuotesQuery]): Result = {
    if (quotes.nonEmpty) Ok(Json.toJson(quotes))
    else notFound("Database is empty!")
  }

  case class FavQuoteForm(csvid: String, favTag: Boolean)
}
