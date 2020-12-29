package forms

import com.krishna.model.Genre.Genre
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
}
