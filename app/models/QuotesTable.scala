package models

trait QuotesTable {

  val quote: String

  val author: String

  val genre: String

  def hasGenre: Boolean = genre.trim.nonEmpty

}
