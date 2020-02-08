package models

import models.Genre.Genre

case class CustomQuoteForm(
    quote: String,
    author: String,
    genre: Genre,
    ownquote: Boolean
) extends QuotesTable
