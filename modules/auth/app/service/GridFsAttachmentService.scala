package service

import com.krishna.util.FutureErrorHandler.ErrorRecover
import com.krishna.util.Logging
import dao.AttachmentDAO
import play.api.mvc.Results.{ BadRequest, Ok, UnsupportedMediaType }
import play.api.mvc.{ MultipartFormData, Result }
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.{ BSONDocument, BSONValue }
import reactivemongo.api.gridfs.ReadFile

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class GridFsAttachmentService @Inject()(
    implicit val executionContext: ExecutionContext,
    val reactiveMongoApi: ReactiveMongoApi
) extends AttachmentDAO
    with Logging {

  /*
    Add a new user profile picture or updates the existing user profile picture
    by deleting the old one
   */
  def addImageAttachment(
      emailId: String,
      file: MultipartFormData.FilePart[ReadFile[BSONValue, BSONDocument]]
  ): Future[Result] = {
    file.contentType
      .fold[Future[Result]](
        Future.successful(
          BadRequest(s"Couldn't find the content type of the attachment for the userId: $emailId")
        )
      ) { mimeType =>
        if (mimeType.startsWith("image")) {
          removeUserPicture(emailId)
            .flatMap(_ => addOrReplaceUserPicture(emailId, file))
        } else {
          val errorMessage: String =
            s"Unsupported MediaType for userId: $emailId with MIME type of ${file.contentType}"
          log.error(errorMessage)
          Future.successful(
            UnsupportedMediaType(errorMessage)
          )
        }
      }
      .errorRecover
  }

  /*
    Add a new picture in the GridFS index or update the existing with a new picture
   */
  def addOrReplaceUserPicture(
      emailId: String,
      file: MultipartFormData.FilePart[ReadFile[BSONValue, BSONDocument]]
  ): Future[Result] = {
    for {
      gfs <- gridFS
      _ <- gfs.update(
        file.ref.id,
        BSONDocument(
          f"$$set" -> BSONDocument("emailId" -> emailId)
        )
      )
    } yield {
      log.info(s"Successfully uploaded the user picture for user id: $emailId")
      Ok(s"Successfully uploaded the user picture for the userId: $emailId")
    }
  }

  /*
    Remove if there is already an existing picture for the requested user emailId
    Only one picture at a time can be stored for each user id
   */
  def removeUserPicture(emailId: String): Future[Unit] = {
    existUserPicture(emailId).map {
      _.fold(
        log.info(s"Picture not found for user id: $emailId")
      ) { record =>
        log.warn(s"Existing picture has been removed for user id: $emailId")
        // Return Unit once the picture is removed from mongoDb
        gridFS.flatMap(_.remove(record.id))
      }
    }
  }

  /*
    Update the email of the record when the user decided to update the email
    So that we won't have old picture for the old email
   */
  def updateEmailInfo(oldEmail: String, newEmail: String): Future[Unit] = {
    existUserPicture(oldEmail).map {
      _.fold(log.info(s"Picture not found for user id: $oldEmail")) { record =>
        log.warn(s"Existing users email id will be replaced by new updated email: $newEmail")
        gridFS.flatMap(
          _.update(
            record.id,
            BSONDocument(
              f"$$set" -> BSONDocument("emailId" -> newEmail)
            )
          )
        )
      }
    }
  }
}
