package models

case class CustomQuoteForm(
  quote: String,
  author: String,
  genre: String,
  ownquote: Boolean
) extends QuotesTable