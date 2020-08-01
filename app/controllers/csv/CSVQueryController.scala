package controllers.csv

import daos.{ CSVQuotesQueryDAO, FavQuoteQueryDAO }
import javax.inject._
import models.CSVQuotesQuery
import models.Genre.Genre
import org.slf4j.LoggerFactory
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * This controller creates an 'Action' to handle HTTP requests to the
  * application's quotes from CSV Query table.
  */
@Singleton
class CSVQueryController @Inject()(
    cc: ControllerComponents,
    csvQuotesDAO: CSVQuotesQueryDAO,
    favQuotesDAO: FavQuoteQueryDAO
)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc) {

  private val logger = LoggerFactory.getLogger(classOf[CSVQueryController])

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
  def getFavQuotes: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    csvQuotesDAO.listAllFavQuotes().map { quoteQueries =>
      Ok(Json.toJson(quoteQueries))
    }
  }

  /**
    * A REST endpoint that gets a random quote as per selected genre from the table.
    */
  def getGenreQuote(genre: Genre): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      csvQuotesDAO.getGenreQuote(genre).map {
        case Some(quote) => Ok(Json.toJson(quote))
        case None =>
          logger.warn(s"Database is empty with that genre: $genre")
          NotFound("Database is empty with that genre!")
      }
  }

  case class FavQuoteForm(csvid: String, favTag: Boolean)
}