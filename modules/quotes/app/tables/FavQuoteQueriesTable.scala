package tables

import com.krishna.model.FavQuoteQuery
import com.krishna.table.{ TableWithCSVId, TableWithUserId }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

class FavQuoteQueriesTable(tag: Tag)
    extends Table[FavQuoteQuery](tag, "fav_quotations")
    with TableWithCSVId[FavQuoteQuery]
    with TableWithUserId[FavQuoteQuery] {

  def favTag: Rep[Boolean] = column[Boolean]("fav_tag")

  def * : ProvenShape[FavQuoteQuery] =
    (id, csvId, userId, favTag) <> ((FavQuoteQuery.apply _).tupled, FavQuoteQuery.unapply)

}

object FavQuoteQueriesTable {
  val favQuoteQueries = TableQuery[FavQuoteQueriesTable]
}
