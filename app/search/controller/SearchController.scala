package search.controller

import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import javax.inject.{ Inject, Singleton }
import play.api.mvc._
import response.ResponseResult
import search.dao.MethodsInEsDAO
import search.util.FutureConv
import utils.Logging

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class SearchController @Inject()(methodsInEsDAO: MethodsInEsDAO, cc: ControllerComponents)(
    implicit executionContext: ExecutionContext
) extends AbstractController(cc)
    with FutureConv[Response[IndexResponse]]
    with ResponseResult
    with Logging {

  def writeInEs(records: Int): Action[AnyContent] = Action.async { implicit request =>
    log.info("Executing writeInEs Controller")

    val listOfFutureResults: Seq[Future[Response[IndexResponse]]] =
      methodsInEsDAO.getAndStoreQuotes(records)

    // Sequence all the futures into a single future of list
    val futureListResults: Future[Seq[Response[IndexResponse]]] =
      Future.sequence(listOfFutureResults)

    futureListResults
      .map { response =>
        Ok("Success")
      }
      //add recover to handle the case where the future fails.
      .recover {
        case e =>
          log.warn(s"${e.getMessage}")
          Ok("failure")
      }
  }

  def deleteIndex(indexName: String): Action[AnyContent] = Action.async { implicit request =>
    log.warn(s"Executing deleteIndex controller for: $indexName")
    log.warn("Hope you know what you are doing!")

    val deleteResult: Future[Response[DeleteIndexResponse]] =
      methodsInEsDAO.deleteQuotesIndex(indexName)

    deleteResult
      .map(responseEsResult)
      .recover {
        case exception =>
          log.error(
            s"Error while deleting an index: $indexName error: ${exception.getMessage}"
          )
          badGateway(s"${exception.getMessage}")
      }
  }

}
