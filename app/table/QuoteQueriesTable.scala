package table

import models.{ QuotesQuery, FavQuoteQuery }
import models.Genre.Genre
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ ForeignKeyQuery, ProvenShape, Tag }
import utils.Implicits._

class QuoteQueriesTable(tag: Tag) extends Table[QuotesQuery](tag, "quotations") {

  def id: Rep[Int]        = column[Int]("id", O.AutoInc)
  def csvid: Rep[String]  = column[String]("csvid", O.PrimaryKey)
  def quote: Rep[String]  = column[String]("quote")
  def author: Rep[String] = column[String]("author")
  def genre: Rep[Genre]   = column[Genre]("genre")

  def * : ProvenShape[QuotesQuery] =
    (id, csvid, quote, author, genre) <> ((QuotesQuery.apply _).tupled, QuotesQuery.unapply)

  // TODO need to use
  def favQuote: ForeignKeyQuery[FavQuoteQueriesTable, FavQuoteQuery] =
    foreignKey("fav_quotations", csvid, FavQuoteQueriesTable.favQuoteQueries)(_.csvid)
}

object QuoteQueriesTable {
  val QuoteQueries = TableQuery[QuoteQueriesTable]
}
