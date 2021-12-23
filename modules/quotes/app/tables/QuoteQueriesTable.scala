package tables

import com.krishna.model.Genre.Genre
import com.krishna.model.{ FavQuoteQuery, QuotesQuery }
import com.krishna.table.TableWithCSVId
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ ForeignKeyQuery, ProvenShape, Tag }
import com.krishna.util.Implicits._

class QuoteQueriesTable(tag: Tag) extends Table[QuotesQuery](tag, "quotes") with TableWithCSVId[QuotesQuery] {

  def quote: Rep[String]          = column[String]("quote")
  def author: Rep[Option[String]] = column[Option[String]]("author")
  def genre: Rep[Option[Genre]]   = column[Option[Genre]]("genre")

  def * : ProvenShape[QuotesQuery] =
    (id, csvId, quote, author, genre) <> ((QuotesQuery.apply _).tupled, QuotesQuery.unapply)

  // A foreign key constraint can be defined with a Table’s foreignKey method
  def favQuote: ForeignKeyQuery[FavQuoteQueriesTable, FavQuoteQuery] =
    foreignKey("fav_quotations", csvId, FavQuoteQueriesTable.favQuoteQueries)(_.csvId)
}

object QuoteQueriesTable {
  val quoteQueries = TableQuery[QuoteQueriesTable]
}
