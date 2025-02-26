package com.krishna.model.base

import com.krishna.model.Genre.Genre

// make it as a sealed trait
trait QuotesTable {

  def quote: String

  def author: Option[String]

  def genre: Option[Genre]

  def hasGenre: Boolean = genre.isDefined

}
