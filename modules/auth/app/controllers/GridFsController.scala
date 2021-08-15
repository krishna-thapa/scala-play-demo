package controllers.auth

import com.krishna.response.ResponseResult
import com.krishna.util.FutureErrorHandler.{ ErrorRecover, ToFuture }
import com.krishna.util.Logging
import config.DecodeHeader
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
    with ResponseResult
    with Logging {

  // Save the user profile picture in the mongo db with email as user id
  def saveUserPicture: Action[MultipartFormData[ReadFile[BSONValue, BSONDocument]]] =
    UserAction.async(gridFSBodyParser(gridFsAttachmentService.gridFS)) { request =>
      DecodeHeader(request.headers) match {
        case Right(user) =>
          log.info(s"Executing saveUserPicture for the request user email: ${user.email}")
          val fileOption: Option[MultipartFormData.FilePart[ReadFile[BSONValue, BSONDocument]]] =
            request.body.files.headOption
          fileOption match {
            case Some(file) =>
              log.info(s"Received file: ${file.filename} with content type of: ${file.contentType}")
              gridFsAttachmentService.addImageAttachment(user.email, file)
            case _ => NotFound("Select the picture to upload").toFuture
          }
        case Left(errorMsg) => responseErrorResult(errorMsg).toFuture
      }
    }

  // Returns a future Result that serves the first matched file, or a NotFound result.
  def getAttachedPicture: Action[AnyContent] = UserAction.async { request =>
    DecodeHeader(request.headers) match {
      case Right(user) =>
        log.info(s"Executing getAttachedPicture for the request user email: ${user.email}")
        getAttachment(user.email)
      case Left(errorMsg) => responseErrorResult(errorMsg).toFuture
    }
  }

  // Removes a attachment picture from index store.
  def removeAttachedPicture: Action[AnyContent] = UserAction.async { request =>
    DecodeHeader(request.headers) match {
      case Right(user) =>
        log.info(s"Executing removeAttachedPicture for the request user email: ${user.email}")
        gridFsAttachmentService
          .removeUserPicture(user.email)
          .map(_ => Ok("Success on removing the profile picture"))
      case Left(errorMsg) => responseErrorResult(errorMsg).toFuture
    }
  }

  private def getAttachment(emailId: String): Future[Result] = {
    gridFsAttachmentService.gridFS.flatMap { gfs =>
      val attachment = gfs.find(BSONDocument("emailId" -> emailId))
      // Content-Disposition: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition
      serve(gfs, emailId)(attachment).errorRecover
    }
  }

}
