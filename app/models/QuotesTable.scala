package models

import models.Genre.Genre

trait QuotesTable {

  def quote: String

  def author: String

  def genre: Option[Genre]

  def hasGenre: Boolean = genre.isDefined

}
