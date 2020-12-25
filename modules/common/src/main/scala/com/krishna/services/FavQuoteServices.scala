package com.krishna.services

import com.krishna.model.base.IdResource

import scala.util.Try

trait FavQuoteServices {

  def listFavQuotes[T <: IdResource](userId: Int): Seq[T]
  def modifyFavQuote[T <: IdResource](userId: Int, csvId: String): Try[T]
}
