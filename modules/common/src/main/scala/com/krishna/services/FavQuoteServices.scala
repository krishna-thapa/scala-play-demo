package com.krishna.services

import com.krishna.model.base.WithCSCVIdResource

import scala.concurrent.Future

trait FavQuoteServices {

  def listFavQuotes[T <: WithCSCVIdResource](userId: Int): Future[Seq[T]]
  def listCachedFavQuotes[T <: WithCSCVIdResource](userId: Int): Future[Seq[T]]
  def modifyFavQuote[T <: WithCSCVIdResource](userId: Int, csvId: String): Future[T]
}
