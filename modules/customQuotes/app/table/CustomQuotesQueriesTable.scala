package table

import java.sql.Date
import models.CustomQuotesQuery
import com.krishna.model.Genre.Genre
import com.krishna.table.{ TableId, TableWithUserId }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import com.krishna.util.Implicits.genreEnumMapper

class CustomQuotesQueriesTable(tag: Tag)
    extends Table[CustomQuotesQuery](tag, "custom_quotations")
    with TableId[CustomQuotesQuery]
    with TableWithUserId[CustomQuotesQuery] {

  def quote: Rep[String] = column[String]("quote")
  def author: Rep[Option[String]] = column[Option[String]]("author")
  def genre: Rep[Option[Genre]] = column[Option[Genre]]("genre")
  def storedDate: Rep[Date] = column[Date]("stored_date")
  def ownQuote: Rep[Boolean] = column[Boolean]("own_quote")

  def * : ProvenShape[CustomQuotesQuery] =
    (id, userId, quote, author, genre, storedDate, ownQuote) <>
      ((CustomQuotesQuery.apply _).tupled, CustomQuotesQuery.unapply)

}

object CustomQuotesQueriesTable {
  val customQuoteQueries = TableQuery[CustomQuotesQueriesTable]
}
