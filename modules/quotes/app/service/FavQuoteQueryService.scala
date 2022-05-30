package service

import com.krishna.model.{ FavQuoteQuery, QuotesQuery }
import com.krishna.util.Logging
import daos.FavQuoteQueryDAO

import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future

@Singleton
class FavQuoteQueryService @Inject() (favQuote: FavQuoteQueryDAO) extends Logging {

  def listAllQuotes(userId: Int): Future[Seq[QuotesQuery]] = {
    favQuote.listFavQuotes[QuotesQuery](userId)
  }

  def createOrUpdateFavQuote(userId: Int, csvId: String): Future[FavQuoteQuery] = {
    favQuote.modifyFavQuote[FavQuoteQuery](userId, csvId)
  }

  def getFavCachedQuotes(userId: Int): Future[Seq[FavQuoteQuery]] = {
    favQuote.listCachedFavQuotes[FavQuoteQuery](userId)
  }

}
