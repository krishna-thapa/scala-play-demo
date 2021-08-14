package controllers.auth

import com.krishna.util.FutureErrorHandler.ErrorRecover
import com.krishna.util.Logging
import dao.MongoControllerRefactored
import depInject.{ SecuredController, SecuredControllerComponents }
import play.api.Configuration
import play.api.mvc.{ Action, AnyContent, MultipartFormData, Result }
import play.modules.reactivemongo.{ ReactiveMongoApi, ReactiveMongoComponents }
import reactivemongo.api.bson.{ BSONDocument, BSONObjectID, BSONValue }
import reactivemongo.api.gridfs.ReadFile
import service.GridFsAttachmentService

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class GridFsController @Inject()(
    scc: SecuredControllerComponents,
    implicit val materializer: akka.stream.Materializer,
    val reactiveMongoApi: ReactiveMongoApi,
    gridFsAttachmentService: GridFsAttachmentService
)(implicit executionContext: ExecutionContext, config: Configuration)
    extends SecuredController(scc)
    with MongoControllerRefactored
    with ReactiveMongoComponents
    with Logging {

  def saveUserPicture(
      emailId: String
  ): Action[MultipartFormData[ReadFile[BSONValue, BSONDocument]]] =
    UserAction.async(gridFSBodyParser(gridFsAttachmentService.gridFS)) { request =>
      log.info(s"Executing saveUserPicture for the request user email id: $emailId")

      val fileOption: Option[MultipartFormData.FilePart[ReadFile[BSONValue, BSONDocument]]] =
        request.body.files.headOption
      fileOption match {
        case Some(file) =>
          log.info(s"Received file: ${file.filename} with content type of: ${file.contentType}")
          gridFsAttachmentService.addImageAttachment(emailId, file)
        case _ => Future.successful(NotFound("Select the picture to upload"))
      }
    }

  // Returns a future Result that serves the first matched file, or a NotFound result.
  def getAttachedPicture(id: String): Action[AnyContent] = UserAction.async { _ =>
    log.info(s"Executing getAttachedPicture for the request attached file id: $id")
    gridFsAttachmentService.parseBSONObjectId(id, getAttachment)
  }

  // Removes a attachment picture from index store.
  def removeAttachedPicture(id: String): Action[AnyContent] = UserAction.async { _ =>
    log.info(s"Executing removeAttachedPicture for the request attached file id: $id")
    gridFsAttachmentService.parseBSONObjectId(id, gridFsAttachmentService.removeAttachment)
  }

  private def getAttachment(id: BSONObjectID): Future[Result] = {
    gridFsAttachmentService.gridFS.flatMap { gfs =>
      val attachment = gfs.find(BSONDocument("_id" -> id))
      // Content-Disposition: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition
      serve(gfs)(attachment).errorRecover
    }
  }

}
