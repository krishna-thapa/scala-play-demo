package scala.daos

import com.krishna.model.base.WithCSCVIdResource
import com.krishna.model.FavQuoteQuery
import com.krishna.services.FavQuoteServices
import com.krishna.util.DbRunner
import play.api.db.slick.DatabaseConfigProvider

import javax.inject.{ Inject, Singleton }
import slick.dbio.Effect
import slick.jdbc.{ JdbcBackend, JdbcProfile }
import slick.jdbc.PostgresProfile.api._
import slick.sql.FixedSqlStreamingAction
import tables.{ FavQuoteQueriesTable, QuoteQueriesTable }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

@Singleton
class FavQuoteQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)
    extends FavQuoteServices
    with DbRunner {

  override val dbConfig: JdbcBackend#DatabaseDef = dbConfigProvider.get[JdbcProfile].db

  /*
    NOTE: Use of Upper bounds using super trait of IdResource: Just to use the scala generic bounds
    In this case, each abstract methods have their own Type so not really need of upper bounds
   */
  /**
    * @param userId primary id from user_details_table
    * @return Quotes that are marked as favorite for the specific user id
    */
  def listFavQuotes[T <: WithCSCVIdResource](userId: Int): Seq[T] = {
    val query = QuoteQueriesTable.quoteQueries
      .join(FavQuoteQueriesTable.favQuoteQueries.filter { favQuote =>
        favQuote.userId === userId && favQuote.favTag
      })
      .on(_.csvId === _.csvId)

    runDbAction(query.result).map(_._1.asInstanceOf[T])
  }

  // list all records from the fav_quotes table
  //def listAllFavQuotes(): Future[Seq[FavQuoteQuery]] = db.run(favQuoteQueries.sortBy(_.id).result)

  /**
    * @param userId primary id from user_details_table
    * @param csvId id from csv custom table
    * @return new or updated records in fav_quotes table
    */
  def modifyFavQuote[T <: WithCSCVIdResource](userId: Int, csvId: String): Try[T] = {
    // check if the record exists with that csv id in the fav_quotes table for that user id
    val favRecord: FixedSqlStreamingAction[Seq[FavQuoteQuery], FavQuoteQuery, Effect.Read] =
      FavQuoteQueriesTable.favQuoteQueries.filter { quote =>
        quote.userId === userId && quote.csvId === csvId
      }.result

    val action =
      favRecord.headOption
        .flatMap {
          case Some(favQuote) =>
            log.info(s"Quote is already in the table with id: ${favQuote.id}")
            alterFavTag(favQuote.id, favQuote.favTag)
            // get the updated record once the fav tag is altered
            favRecord.head
          case None =>
            log.info("Inserting new record in the fav quotes table")
            createFavQuote(userId, csvId)
        }
    runDbActionCatchError(action).map(_.asInstanceOf[T])
  }

  /**
    * @param id id from fav_quotations table
    * @param tag boolean tag to specify favorite quote
    * @return record with altered fav tag
    */
  private def alterFavTag(id: Int, tag: Boolean): Int = {
    log.info(s"Changing the fav tag status of: $id to ${!tag}")
    runDbAction(
      FavQuoteQueriesTable.favQuoteQueries
        .filter(_.id === id)
        .map(quote => quote.favTag)
        .update(!tag)
    )
  }

  /**
    * @param userId primary id from user_details_table
    * @param csvId id from csv custom table
    * @return create a new record in the fav_quotes table with fav tag as true
    */
  private def createFavQuote(
      userId: Int,
      csvId: String
  ): DBIOAction[FavQuoteQuery, NoStream, Effect.Write] = {
    val insertFavQuote = FavQuoteQueriesTable.favQuoteQueries returning
      FavQuoteQueriesTable.favQuoteQueries.map(_.id) into (
        (
            fields,
            id
        ) => fields.copy(id = id)
    )
    insertFavQuote += FavQuoteQuery(
      0,
      userId = userId,
      csvId = csvId,
      favTag = true
    )
  }
}
