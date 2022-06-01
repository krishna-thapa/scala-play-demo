package com.krishna.util

import slick.dbio.Effect.All
import slick.jdbc.JdbcBackend
import slick.dbio.{ DBIOAction, NoStream }
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, Future }
import scala.util.{ Failure, Success, Try }

trait DbRunner extends Logging {

  val dbConfig: JdbcBackend#DatabaseDef

  // return result from future
  implicit class FutureResult[T](future: Future[T]) {

    def andGetResult(timeout: Option[Int] = None): T =
      Await.result(future, timeout.getOrElse(60).seconds)

  }

  /* Run and get the result from the future */
  // TODO: REMOVE THIS METHOD
  def runDbAction[T](action: DBIOAction[T, NoStream, All], timeout: Option[Int] = None): T = {
    dbConfig.run(action.transactionally).andGetResult(timeout)
  }

  /* Run and get the result asynchronously */
  def runDbAsyncAction[T](action: DBIOAction[T, NoStream, All]): Future[T] = {
    dbConfig.run(action.transactionally)
  }

  /* Run and get the result from the future or catch any error*/
  // TODO: REMOVE THIS METHOD
  def runDbActionCatchError[T](
    action: DBIOAction[T, NoStream, All]
  ): Future[T] = {
    val runAsyncAction: Try[Future[T]] =
      try {
        Success(runDbAsyncAction(action))
      } catch {
        case ex: Exception =>
          log.warn("Db Action failed: ", ex)
          Failure(ex)
      }
    Future.fromTry(runAsyncAction).flatten
  }

}
