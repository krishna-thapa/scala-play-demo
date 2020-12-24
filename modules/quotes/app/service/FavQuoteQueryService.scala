package service

import com.krishna.model.{ FavQuoteQuery, QuotesQuery }
import com.krishna.util.Logging
import daos.FavQuoteQueryDAO

import javax.inject.{ Inject, Singleton }
import scala.util.Try

@Singleton
class FavQuoteQueryService @Inject()(favQuote: FavQuoteQueryDAO) extends Logging {

  def listAllQuotes(userId: Int): Seq[QuotesQuery] = {
    favQuote.listFavQuotes[QuotesQuery](userId)
  }

  def createOrUpdateFavQuote(userId: Int, csvId: String): Try[FavQuoteQuery] = {
    favQuote.modifyFavQuote[FavQuoteQuery](userId, csvId)
  }
}
