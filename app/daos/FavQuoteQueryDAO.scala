package daos

import javax.inject.{ Inject, Singleton }
import models.FavQuoteQuery
import org.slf4j.LoggerFactory
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class FavQuoteQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(
    implicit executionContext: ExecutionContext
) {

  private val logger = LoggerFactory.getLogger(classOf[FavQuoteQueryDAO])

  lazy val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class FavQuoteQueriesTable(tag: Tag) extends Table[FavQuoteQuery](tag, "fav_quotations") {
    def id: Rep[Int]         = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def csvid: Rep[String]   = column[String]("csvid")
    def favTag: Rep[Boolean] = column[Boolean]("favtag")
    def * : ProvenShape[FavQuoteQuery] =
      (id, csvid, favTag) <> ((FavQuoteQuery.apply _).tupled, FavQuoteQuery.unapply)
  }

  private val favQuoteQueries = TableQuery[FavQuoteQueriesTable]

  // list all records from the fav_quotes table
  //def listAllFavQuotes(): Future[Seq[FavQuoteQuery]] = db.run(favQuoteQueries.sortBy(_.id).result)

  /**
    * @param csvid id from csv custom table
    * @return new or updated records in fav_quotes table
    */
  def modifyFavquote(csvid: String): Future[FavQuoteQuery] = {
    // check the record exists with that csv id in the fav_quotes table
    val action = favQuoteQueries.filter(_.csvid === csvid).result.headOption.flatMap {
      case Some(favQuote) =>
        logger.info(s"Quote is already in the table with id: ${favQuote.id}")
        alterFavTag(favQuote.csvid, favQuote.favTag)
        // get the updated record once the fav tag is altered
        favQuoteQueries.filter(_.csvid === csvid).result.head
      case None =>
        logger.info("Inserting new record in the fav quotes table")
        createFavquote(csvid)
    }
    db.run(action)
  }

  /**
    * @param csvid id from csv custom table
    * @param tag boolean tag to specify favorite quote
    * @return record with altered fav tag
    */
  def alterFavTag(csvid: String, tag: Boolean): Future[Int] = {
    logger.info(s"Changing the fav tag status of: $csvid to ${!tag}")
    db.run(favQuoteQueries.filter(_.csvid === csvid).map(quote => quote.favTag).update(!tag))
  }

  /**
    * @param csvid id from csv custom table
    * @return create a new record in the fav_quotes table with fav tag as true
    */
  def createFavquote(csvid: String) = {
    val insertFavQuote = favQuoteQueries returning favQuoteQueries.map(_.id) into (
        (
            fields,
            id
        ) => fields.copy(id = id)
    )
    insertFavQuote += FavQuoteQuery(
      0,
      csvid = csvid,
      favTag = true
    )
  }
}
