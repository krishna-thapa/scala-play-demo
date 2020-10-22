package search.controller

import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import javax.inject.{ Inject, Singleton }
import play.api.mvc._
import search.dao.MethodsInEsDAO
import search.util.FutureConv
import utils.Logging

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

@Singleton
class SearchController @Inject()(methodsInEsDAO: MethodsInEsDAO, cc: ControllerComponents)(
    implicit executionContext: ExecutionContext
) extends AbstractController(cc)
    with FutureConv[Response[IndexResponse]]
    with Logging {

  def writeInEs(records: Int): Action[AnyContent] = Action { implicit request =>
    log.info("Executing getFirst10Quotes")
    val futureListOfResults = futureListOfTrys(methodsInEsDAO.getAndStoreQuotes(records))
    val foo = futureListOfResults.onComplete {
      case Success(result) =>
        log.info(s"demo: ${result.head}")
        Ok("Sucess")
      case Failure(exception) =>
        log.error(s"error: ${exception.getMessage}")
        Ok("Sucess")
    }
    Ok("Sucess")
  }

  /*  def deleteIndex: Action[AnyContent] = Action { implicit request =>
    log.info("Deleting the entire index")
    methodsInEsDAO.deleteIndex
  }*/

}
