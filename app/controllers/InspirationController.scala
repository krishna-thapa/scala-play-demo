package controllers

import javax.inject._
import play.api.libs.json._
import play.api.mvc._

/**
 * This controller creates an 'Action' to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class InspirationController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  val y: JsValue = Json.arr(
    Json.obj("quote" -> "Make your life a masterpiece, imagine no limitations on what you can be, have or do.", "author" -> "Brian Tracy"),
    Json.obj("quote" -> "We may encounter many defeats but we must not be defeated.", "author" -> "Maya Angelou"),
    Json.obj("quote" -> "I am not a product of my circumstances. I am a product of my decisions.", "author" -> "Stephen Covey"),
    Json.obj("quote" -> "We must let go of the life we have planned, so as to accept the one that is waiting for us.", "author" -> "Joseph Campbell"),
    Json.obj("quote" -> "Believe you can and you're halfway there.", "author" -> "Theodore Roosevelt"),
    Json.obj("quote" -> "We know what we are, but know not what we may be.", "author" -> "William Shakespeare"),
    Json.obj("quote" -> "We can't help everyone, but everyone can help someone.", "author" -> "Ronald Reagan"),
    Json.obj("quote" -> "When you have a dream, you've got to grab it an never let go.", "author" -> "Carol Burnett"),
    Json.obj("quote" -> "Your present circumstances don't determine where you can go; they merely determine where you start.", "author" -> "Nido Quebein"),
    Json.obj("quote" -> "Thinking: the talking of the soul with itself.", "author" -> "Plato")
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(generateQuote(y, scala.util.Random.nextInt(10)))
  }

  def generateQuote(quotes: JsValue, random: Int): String = {
    val quote: JsValue = (quotes(random)\"quote").get
    val author: JsValue = (quotes(random)\"author").get
    author.as[String] + ": " + quote.as[String]
  }

}
