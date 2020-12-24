package com.krishna.services

import com.krishna.model.base.QuoteResource

import scala.util.Try

trait FavQuoteServices {

  def listFavQuotes[T <: QuoteResource](userId: Int): Seq[T]
  def modifyFavQuote[T <: QuoteResource](userId: Int, csvId: String): Try[T]
}
