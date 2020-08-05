package forms

import models.Genre.Genre
import models.QuotesTable

case class CustomQuoteForm(
    quote: String,
    author: String,
    genre: Option[Genre] = None,
    ownquote: Boolean
) extends QuotesTable
