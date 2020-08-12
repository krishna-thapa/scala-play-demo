package forms

import models.Genre.Genre
import models.QuotesTable

final case class CustomQuoteForm(
    quote: String,
    author: String,
    genre: Option[Genre] = None,
    ownquote: Boolean
) extends QuotesTable
