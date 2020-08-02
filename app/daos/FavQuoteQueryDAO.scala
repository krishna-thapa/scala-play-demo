package daos

import javax.inject.{ Inject, Singleton }
import models.FavQuoteQuery
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.dbio.Effect
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import table.FavQuoteQueriesTable
import utils.{ DbRunner, Logging }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

@Singleton
class FavQuoteQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)
    extends DbRunner
    with Logging {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  // list all records from the fav_quotes table
  //def listAllFavQuotes(): Future[Seq[FavQuoteQuery]] = db.run(favQuoteQueries.sortBy(_.id).result)

  /**
    * @param csvid id from csv custom table
    * @return new or updated records in fav_quotes table
    */
  def modifyFavQuote(csvid: String): Try[FavQuoteQuery] = {
    // check if the record exists with that csv id in the fav_quotes table
    val action =
      FavQuoteQueriesTable.favQuoteQueries.filter(_.csvid === csvid).result.headOption.flatMap {
        case Some(favQuote) =>
          log.info(s"Quote is already in the table with id: ${favQuote.id}")
          alterFavTag(favQuote.csvid, favQuote.favTag)
          // get the updated record once the fav tag is altered
          FavQuoteQueriesTable.favQuoteQueries.filter(_.csvid === csvid).result.head
        case None =>
          log.info("Inserting new record in the fav quotes table")
          createFavQuote(csvid)
      }
    runDbActionCatchError(action)
  }

  /**
    * @param csvid id from csv custom table
    * @param tag boolean tag to specify favorite quote
    * @return record with altered fav tag
    */
  def alterFavTag(csvid: String, tag: Boolean): Int = {
    log.info(s"Changing the fav tag status of: $csvid to ${!tag}")
    runDbAction(
      FavQuoteQueriesTable.favQuoteQueries
        .filter(_.csvid === csvid)
        .map(quote => quote.favTag)
        .update(!tag)
    )
  }

  /**
    * @param csvid id from csv custom table
    * @return create a new record in the fav_quotes table with fav tag as true
    */
  def createFavQuote(csvid: String): DBIOAction[FavQuoteQuery, NoStream, Effect.Write] = {
    val insertFavQuote = FavQuoteQueriesTable.favQuoteQueries returning
      FavQuoteQueriesTable.favQuoteQueries.map(_.id) into (
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
