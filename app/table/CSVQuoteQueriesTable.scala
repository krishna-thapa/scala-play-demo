package table

import models.{ CSVQuotesQuery, FavQuoteQuery, Genre }
import models.Genre.Genre
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ ForeignKeyQuery, ProvenShape, Tag }
import utils.Implicits._

class CSVQuoteQueriesTable(tag: Tag) extends Table[CSVQuotesQuery](tag, "quotations") {

  def id: Rep[Int]        = column[Int]("id", O.AutoInc)
  def csvid: Rep[String]  = column[String]("csvid", O.PrimaryKey)
  def quote: Rep[String]  = column[String]("quote")
  def author: Rep[String] = column[String]("author")
  def genre: Rep[Genre]   = column[Genre]("genre")

  def * : ProvenShape[CSVQuotesQuery] =
    (id, csvid, quote, author, genre) <> ((CSVQuotesQuery.apply _).tupled, CSVQuotesQuery.unapply)

  // TODO need to use
  def favQuote: ForeignKeyQuery[FavQuoteQueriesTable, FavQuoteQuery] =
    foreignKey("fav_quotations", csvid, FavQuoteQueriesTable.favQuoteQueries)(_.csvid)
}

object CSVQuoteQueriesTable {
  val CSVQuoteQueries = TableQuery[CSVQuoteQueriesTable]
}
