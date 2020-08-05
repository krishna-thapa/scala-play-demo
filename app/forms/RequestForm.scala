package forms

import models.Genre.Genre
import play.api.data.Forms._
import play.api.data.{ Form, Forms }

object RequestForm {

  /**
    * The mapping for the QuotesQuery form.
    */
  val quotesQueryForm: Form[CustomQuoteForm] = Form {
    mapping(
      "quote"    -> nonEmptyText.verifying(_.nonEmpty),
      "author"   -> nonEmptyText,
      "genre"    -> optional(Forms.of[Genre]),
      "ownquote" -> boolean
    )(CustomQuoteForm.apply)(CustomQuoteForm.unapply)
  }
}
