package service

import com.krishna.model.{ FavQuoteQuery, QuotesQuery }
import com.krishna.util.Logging
import daos.FavQuoteQueryDAO

import java.util.UUID
import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future

@Singleton
class FavQuoteQueryService @Inject() (favQuote: FavQuoteQueryDAO) extends Logging {

  def listAllQuotes(userId: UUID): Future[Seq[QuotesQuery]] = {
    favQuote.listFavQuotes[QuotesQuery](userId)
  }

  def createOrUpdateFavQuote(userId: UUID, csvId: String): Future[FavQuoteQuery] = {
    favQuote.modifyFavQuote[FavQuoteQuery](userId, csvId)
  }

  def getFavCachedQuotes(userId: UUID): Future[Seq[FavQuoteQuery]] = {
    favQuote.listCachedFavQuotes[FavQuoteQuery](userId)
  }

}
