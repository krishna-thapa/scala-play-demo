package table

import java.sql.Date

import models.CustomQuotesQuery
import com.krishna.model.Genre.Genre
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import com.krishna.util.Implicits.genreEnumMapper

class CustomQuotesQueriesTable(tag: Tag)
    extends Table[CustomQuotesQuery](tag, "custom_quotations") {

  def id: Rep[Int]              = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def quote: Rep[String]        = column[String]("quote")
  def author: Rep[String]       = column[String]("author")
  def genre: Rep[Option[Genre]] = column[Option[Genre]]("genre")
  def storeddate: Rep[Date]     = column[Date]("storeddate")
  def ownquote: Rep[Boolean]    = column[Boolean]("ownquote")
  def * : ProvenShape[CustomQuotesQuery] =
    (id, quote, author, genre, storeddate, ownquote) <>
      ((CustomQuotesQuery.apply _).tupled, CustomQuotesQuery.unapply)
}

object CustomQuotesQueriesTable {
  val customQuoteQueries = TableQuery[CustomQuotesQueriesTable]
}
