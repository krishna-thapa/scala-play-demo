package com.krishna.akkaStream

import akka.NotUsed
import akka.stream.scaladsl.{ Flow, Sink, Source }
import slick.dbio.{ DBIOAction, Effect, Streaming }
import com.krishna.util.DbRunner
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{ ResultSetConcurrency, ResultSetType }

import scala.concurrent.Future

trait StreamsInit extends DbRunner {

  def batchSize: Int

  /**
    * Create an akka-streams Source from a reactive-streams publisher,
    * entering akka-streams land where we get access to a richer API for stream element processing
    * Execution of the DBIOAction does not start until a Subscriber is attached to the stream.
    * Note: https://scala-slick.org/doc/3.2.0/dbio.html#streaming
    */
  def recordsSource[T](
    getRecords: DBIOAction[Seq[T], Streaming[T], Effect.Read]
  ): Source[T, NotUsed] = Source.fromPublisher {
    {
      dbConfig.stream {
        getRecords
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = batchSize
          )
          .transactionally
      }
    }
  }

  /**
    * Construct a Flow[R, T] that emits an elements of R from a function
    * that returns a Future[T].
    * Parallelism of the Future-producing call is controlled under the hood by the actor
    * behind the Flow.
    */
  def addFlowPerRecord[R, T](
    flowMethod: R => Future[T]
  ): Flow[R, T, NotUsed] = {
    Flow[R].mapAsync(parallelism = 5) { record =>
      flowMethod(record)
    }
  }

  /**
    * Sink that indiscriminately tallies up and prints the count of elements it has seen.
    */
  def logElementsPerBlock[T]: Sink[T, Future[Int]] = {
    Sink.fold[Int, T](0) { (sum, _) =>
      val newSum = sum + 1
      if (newSum % batchSize == 0) {
        log.info(s"\rCount for each batch: $newSum")
      }
      newSum
    }
  }

}
