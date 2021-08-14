package dao

import com.krishna.util.FutureErrorHandler.ErrorRecover
import com.krishna.util.Logging
import play.api.mvc.Result
import play.api.mvc.Results.BadRequest
import play.modules.reactivemongo.ReactiveMongoComponents
import reactivemongo.api.bson.{ BSONDocument, BSONObjectID }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import scala.util.control.NonFatal

trait AttachmentDAO extends ReactiveMongoComponents with Logging {

  implicit val executionContext: ExecutionContext

  /*
    Create a needed indexes on the GridFS collections (chunks and files) with a default prefix of "fs"
    Please note that you should really consider reading http://www.mongodb.org/display/DOCS/Indexes
    before doing this, especially in production.
   */
  def gridFS: Future[MongoControllerRefactored.GridFS] =
    (for {
      attachments <- reactiveMongoApi.asyncGridFS
      _ <- attachments.ensureIndex().map { index =>
        // let's build an index on our gridfs chunks collection if none
        log.info(s"Checked if index is already created, result is $index")
      }
    } yield attachments).recover {
      case NonFatal(error) =>
        println("Error while creating index for GridFS: " + error.getMessage)
        throw error
    }

  def existUserPicture(emailId: String) = {
    gridFS.flatMap(_.find(BSONDocument("emailId" -> emailId)).headOption)
  }

  def parseBSONObjectId(
      id: String,
      callService: BSONObjectID => Future[Result]
  ): Future[Result] = {
    BSONObjectID.parse(id) match {
      case Success(objectId) =>
        callService(objectId).errorRecover
      case Failure(exception) =>
        Future.successful(
          BadRequest(s"Cannot parse the id: $id, error with: ${exception.getMessage}")
        )
    }
  }

}
