package daos

import com.krishna.model.{ FavQuoteQuery, QuotesQuery }
import com.krishna.util.{ DbRunner, Logging }
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.dbio.Effect
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import tables.{ FavQuoteQueriesTable, QuoteQueriesTable }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

@Singleton
class FavQuoteQueryDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)
    extends CommonMethods[QuotesQuery]
    with DbRunner
    with Logging {

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  /**
    * @return Quotes that are marked as favorite
    */
  def listAllQuotes(): Seq[QuotesQuery] = {
    val query = QuoteQueriesTable.quoteQueries
      .join(FavQuoteQueriesTable.favQuoteQueries.filter(_.favTag))
      .on(_.csvId === _.csvId)

    runDbAction(query.result).map(_._1)
  }

  // list all records from the fav_quotes table
  //def listAllFavQuotes(): Future[Seq[FavQuoteQuery]] = db.run(favQuoteQueries.sortBy(_.id).result)

  /**
    * @param csvId id from csv custom table
    * @return new or updated records in fav_quotes table
    */
  def modifyFavQuote(csvId: String): Try[FavQuoteQuery] = {
    // check if the record exists with that csv id in the fav_quotes table
    val action =
      FavQuoteQueriesTable.favQuoteQueries.filter(_.csvId === csvId).result.headOption.flatMap {
        case Some(favQuote) =>
          log.info(s"Quote is already in the table with id: ${favQuote.id}")
          alterFavTag(favQuote.csvId, favQuote.favTag)
          // get the updated record once the fav tag is altered
          FavQuoteQueriesTable.favQuoteQueries.filter(_.csvId === csvId).result.head
        case None =>
          log.info("Inserting new record in the fav quotes table")
          createFavQuote(csvId)
      }
    runDbActionCatchError(action)
  }

  /**
    * @param csvId id from csv custom table
    * @param tag boolean tag to specify favorite quote
    * @return record with altered fav tag
    */
  def alterFavTag(csvId: String, tag: Boolean): Int = {
    log.info(s"Changing the fav tag status of: $csvId to ${!tag}")
    runDbAction(
      FavQuoteQueriesTable.favQuoteQueries
        .filter(_.csvId === csvId)
        .map(quote => quote.favTag)
        .update(!tag)
    )
  }

  /**
    * @param csvId id from csv custom table
    * @return create a new record in the fav_quotes table with fav tag as true
    */
  def createFavQuote(csvId: String): DBIOAction[FavQuoteQuery, NoStream, Effect.Write] = {
    val insertFavQuote = FavQuoteQueriesTable.favQuoteQueries returning
      FavQuoteQueriesTable.favQuoteQueries.map(_.id) into (
        (
            fields,
            id
        ) => fields.copy(id = id)
    )
    insertFavQuote += FavQuoteQuery(
      0,
      csvId = csvId,
      favTag = true
    )
  }
}
