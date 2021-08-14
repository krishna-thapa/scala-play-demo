package dao

import scala.concurrent.{ ExecutionContext, Future }
import akka.stream.Materializer
import com.krishna.util.Logging
import play.api.mvc.{ BodyParser, MultipartFormData, ResponseHeader, Result }
import play.api.http.{ HttpChunk, HttpEntity }
import play.modules.reactivemongo.{ PlaySupport, ReactiveMongoComponents }
import reactivemongo.api.{ DB, MongoConnection }
import reactivemongo.api.bson.{ BSONDocument, BSONValue }
import reactivemongo.api.bson.collection.BSONSerializationPack

object MongoControllerRefactored {
  type GridFS = reactivemongo.api.gridfs.GridFS[BSONSerializationPack.type]

  type GridFSBodyParser[T <: BSONValue] =
    BodyParser[MultipartFormData[reactivemongo.api.gridfs.ReadFile[T, BSONDocument]]]

  type FileToSave[T <: BSONValue] = reactivemongo.api.gridfs.FileToSave[T, BSONDocument]

  /** `Content-Disposition: attachment` */
  private[dao] val CONTENT_DISPOSITION_ATTACHMENT = "attachment"

  /** `Content-Disposition: inline` */
  private[dao] val CONTENT_DISPOSITION_INLINE = "inline"

}

/** A mixin for controllers that will provide MongoDB actions. */
trait MongoControllerRefactored extends PlaySupport.Controller with Logging {
  self: ReactiveMongoComponents =>

  import reactivemongo.api.Cursor
  import reactivemongo.akkastream.GridFSStreams
  import MongoControllerRefactored._

  /** Returns the current MongoConnection instance (the connection pool manager).
    */
  protected final def connection: MongoConnection = reactiveMongoApi.connection

  /** Returns the default database (as specified in `application.conf`). */
  protected final def database: Future[DB] = reactiveMongoApi.database

  /** Returns a future Result that serves the first matched file, or a `NotFound` result.
    */
  protected final def serve[Id <: BSONValue](gfs: GridFS)(
      foundFile: Cursor[gfs.ReadFile[Id]],
      dispositionMode: String = CONTENT_DISPOSITION_INLINE
  )(implicit materializer: Materializer): Future[Result] = {
    implicit def ec: ExecutionContext = materializer.executionContext

    foundFile.headOption
      .collect {
        case Some(file) =>
          file
      }
      .map { file =>
        def filename    = file.filename.getOrElse("file.bin")
        def contentType = file.contentType.getOrElse("application/octet-stream")

        def chunks = GridFSStreams(gfs).source(file).map(HttpChunk.Chunk)

        Result(
          header = ResponseHeader(OK),
          body = HttpEntity.Chunked(chunks, Some(contentType))
        ).as(contentType)
          .withHeaders(
            CONTENT_LENGTH -> file.length.toString,
            CONTENT_DISPOSITION -> (s"""$dispositionMode; filename="$filename"; filename*="UTF-8''""" + java.net.URLEncoder
              .encode(filename, "UTF-8")
              .replace("+", "%20") + '"')
          )

      }
      .recover {
        case _ =>
          NotFound
      }
  }

  protected final def gridFSBodyParser(
      gfs: Future[GridFS]
  )(implicit materializer: Materializer): GridFSBodyParser[BSONValue] = {
    implicit def ec: ExecutionContext = materializer.executionContext
    import play.api.libs.streams.Accumulator

    parse.multipartFormData {
      case PlaySupport.FileInfo(partName, filename, Some(contentType))
          if contentType.startsWith("image") =>
        Accumulator.flatten(gfs.map { gridFS =>
          val fileRef = gridFS.fileToSave( // see Api.scala
            filename = Some(filename),
            contentType = Option(contentType)
          )

          val sink = GridFSStreams(gridFS).sinkWithMD5(fileRef)

          Accumulator(sink).map { ref =>
            MultipartFormData.FilePart(partName, filename, Option(contentType), ref)
          }
        })

      case info =>
        val errorMessage: String =
          s"Unsupported: ${info.contentType} for the uploaded file ${info.fileName}"
        log.error(errorMessage)
        sys.error(errorMessage)
    }
  }
}
