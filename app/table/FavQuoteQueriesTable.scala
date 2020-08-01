package table

import models.FavQuoteQuery
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

class FavQuoteQueriesTable(tag: Tag) extends Table[FavQuoteQuery](tag, "fav_quotations") {
  def id: Rep[Int]         = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def csvid: Rep[String]   = column[String]("csvid")
  def favTag: Rep[Boolean] = column[Boolean]("favtag")

  def * : ProvenShape[FavQuoteQuery] =
    (id, csvid, favTag) <> ((FavQuoteQuery.apply _).tupled, FavQuoteQuery.unapply)
}

object FavQuoteQueriesTable {
  val favQuoteQueries = TableQuery[FavQuoteQueriesTable]
}
