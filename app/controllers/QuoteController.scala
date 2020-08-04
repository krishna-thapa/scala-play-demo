package controllers

import daos.{ FavQuoteQueryDAO, QuoteQueryDAO }
import helper.ResponseMethod
import javax.inject._
import models.Genre.Genre
import play.api.mvc._
import utils.Logging

import scala.concurrent.ExecutionContext
import scala.util.matching.Regex

/**
  * This controller creates an 'Action' to handle HTTP requests to the
  * application's quotes from CSV Query table.
  */
@Singleton
class QuoteController @Inject()(
    cc: ControllerComponents,
    quotesDAO: QuoteQueryDAO,
    favQuotesDAO: FavQuoteQueryDAO
)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc)
    with ResponseMethod
    with Logging {

  protected lazy val csvIdPattern: Regex = "CSV[0-9]+$".r

  /**
    * A REST endpoint that gets a random quote as JSON from CSV quotes table.
    */
  def getRandomQuote: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getRandomQuote")
    responseSeqResult(quotesDAO.randomQuote(1))
  }

  /**
    * A REST endpoint that gets all the quotes as JSON from CSV quotes table.
    */
  def getAllQuotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getAllQuotes")
    responseSeqResult(quotesDAO.allQuotes())
  }

  /**
    * A REST endpoint that gets random 10 quotes as JSON from CSV quotes table.
    */
  def getFirst10Quotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getFirst10Quotes")
    responseSeqResult(quotesDAO.randomQuote(10))
  }

  /**
    * A REST endpoint that creates or altered the fav tag in the fav_quotes table.
    */
  def favQuote(csvid: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      log.info("Executing favQuote")
      if (csvIdPattern.matches(csvid)) {
        responseTryResult(favQuotesDAO.modifyFavQuote(csvid))
      } else {
        badRequest("Id of quote should be in CSV123 format!")
      }
  }

  /**
    * A REST endpoint that gets all favorite quotes as JSON.
    */
  def getFavQuotes: Action[AnyContent] = Action { implicit request =>
    log.info("Executing getFavQuotes")
    responseSeqResult(quotesDAO.listAllFavQuotes())
  }

  /**
    * A REST endpoint that gets a random quote as per selected genre from the table.
    */
  def getGenreQuote(genre: Genre): Action[AnyContent] = Action { implicit request =>
    log.info("Executing getGenreQuote")
    responseOptionResult(quotesDAO.getGenreQuote(genre))
  }

}
