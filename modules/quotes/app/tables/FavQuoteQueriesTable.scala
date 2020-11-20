package tables

import com.krishna.model.FavQuoteQuery
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

class FavQuoteQueriesTable(tag: Tag) extends Table[FavQuoteQuery](tag, "fav_quotations") {
  def id: Rep[Int]         = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def csvId: Rep[String]   = column[String]("csv_id")
  def favTag: Rep[Boolean] = column[Boolean]("fav_tag")

  def * : ProvenShape[FavQuoteQuery] =
    (id, csvId, favTag) <> ((FavQuoteQuery.apply _).tupled, FavQuoteQuery.unapply)
}

object FavQuoteQueriesTable {
  val favQuoteQueries = TableQuery[FavQuoteQueriesTable]
}
