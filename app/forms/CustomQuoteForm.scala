package forms

import com.krishna.model.Genre.Genre
import com.krishna.model.base.QuotesTable

final case class CustomQuoteForm(
    quote: String,
    author: String,
    genre: Option[Genre] = None,
    ownQuote: Boolean
) extends QuotesTable
