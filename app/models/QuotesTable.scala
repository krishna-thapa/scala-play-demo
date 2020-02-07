package models

import models.Genre.Genre

trait QuotesTable {

  val quote: String

  val author: String

  val genre: Genre

  //def hasGenre: Boolean = genre

}
