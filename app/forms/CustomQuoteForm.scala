package forms

import com.krishna.model.QuotesTable
import com.krishna.model.Genre.Genre

final case class CustomQuoteForm(
    quote: String,
    author: String,
    genre: Option[Genre] = None,
    ownquote: Boolean
) extends QuotesTable
