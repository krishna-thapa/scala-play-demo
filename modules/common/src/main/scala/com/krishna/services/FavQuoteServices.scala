package com.krishna.services

import com.krishna.model.base.WithCSCVIdResource

import scala.util.Try

trait FavQuoteServices {

  def listFavQuotes[T <: WithCSCVIdResource](userId: Int): Seq[T]
  def modifyFavQuote[T <: WithCSCVIdResource](userId: Int, csvId: String): Try[T]
}
