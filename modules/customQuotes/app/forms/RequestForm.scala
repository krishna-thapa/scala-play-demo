package forms

import com.krishna.model.Genre.Genre
import com.krishna.model.base.QuotesTable
import play.api.data.Forms._
import play.api.data.{ Form, Forms }

object RequestForm {

  /**
    * The mapping for the QuotesQuery searchForm.
    */
  val quotesQueryForm: Form[CustomQuoteForm] = Form {
    mapping(
      "quote"    -> nonEmptyText.verifying(_.nonEmpty),
      "author"   -> optional(text),
      "genre"    -> optional(Forms.of[Genre]),
      "ownQuote" -> boolean
    )(CustomQuoteForm.apply)(CustomQuoteForm.unapply)
  }

  case class CustomQuoteForm(
      quote: String,
      author: Option[String],
      genre: Option[Genre] = None,
      ownQuote: Boolean = false
  ) extends QuotesTable
}
