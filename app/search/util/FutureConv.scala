package search.util

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

/*
  List[Future] to Future[List] disregarding failed futures
  The trick is to first make sure that none of the futures has failed. .recover is
  your friend here, you can combine it with map to convert all the Future[T] results
  to Future[Try[T]]] instances, all of which are certain to be successful futures.

  note: You can use Option or Either as well here, but Try is the cleanest way
  if you specifically want to trap exceptions

  https://stackoverflow.com/questions/20874186/scala-listfuture-to-futurelist-disregarding-failed-futures

 */

trait FutureConv[T] {

  def futureToFutureTry(f: Future[T]): Future[Try[T]] = {
    f.map(Success(_)).recover { case x => Failure(x) }
  }

  // Then use Future.sequence as before, to give you a Future[List[Try[T]]]
  def futureListOfTrys(listOfFutures: Seq[Future[T]]): Future[Seq[Try[T]]] = {
    val listOfFutureTrys: Seq[Future[Try[T]]] = listOfFutures.map(futureToFutureTry)
    Future.sequence(listOfFutureTrys)
  }

}
