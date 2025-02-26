package com.krishna.services

import com.krishna.model.base.WithCSCVIdResource

import java.util.UUID
import scala.concurrent.Future

trait FavQuoteServices {

  def listFavQuotes[T <: WithCSCVIdResource](userId: UUID): Future[Seq[T]]
  def listCachedFavQuotes[T <: WithCSCVIdResource](userId: UUID): Future[Seq[T]]
  def modifyFavQuote[T <: WithCSCVIdResource](userId: UUID, csvId: String): Future[T]
}
