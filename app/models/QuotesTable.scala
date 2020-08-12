package models

import models.Genre.Genre

// make it as a sealed trait
trait QuotesTable {

  def quote: String

  def author: String

  def genre: Option[Genre]

  def hasGenre: Boolean = genre.isDefined

}
