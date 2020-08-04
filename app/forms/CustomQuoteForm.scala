package forms

import models.Genre.Genre
import models.QuotesTable

case class CustomQuoteForm(
    quote: String,
    author: String,
    genre: Genre,
    ownquote: Boolean
) extends QuotesTable
