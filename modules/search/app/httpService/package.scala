import models.AuthorDetails

package object httpService {
  case class FinalQuotes(quote: String, author: Option[String], genre: Seq[String])

  case class FinalQuotesWithAuthor(quoteDetails: FinalQuotes, authorDetails: Option[AuthorDetails] = None)
}
