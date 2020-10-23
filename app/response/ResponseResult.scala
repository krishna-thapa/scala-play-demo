package response

import com.sksamuel.elastic4s.Response
import play.api.libs.json.{ Json, OFormat }
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import response.ResponseMsg.{ EmptyDbMsg, InvalidDate }

import scala.util.{ Failure, Success, Try }

trait ResponseResult extends ResponseError {

  def responseOk[T](result: T)(implicit conv: OFormat[T]): Result = {
    Ok(Json.toJson(result))
  }

  def responseSeqResult[T](records: Seq[T])(implicit conv: OFormat[T]): Result = {
    if (records.nonEmpty) Ok(Json.toJson(records))
    else notFound(EmptyDbMsg.msg)
  }

  // TODO have to remove it
  def responseSeqString(records: Seq[String]): Result = {
    if (records.nonEmpty) Ok(Json.toJson(records))
    else notFound(EmptyDbMsg.msg)
  }

  def responseOptionResult[T](record: Option[T])(implicit conv: OFormat[T]): Result = {
    record match {
      case Some(quote) => Ok(Json.toJson(quote))
      case None =>
        notFound(EmptyDbMsg.msg)
    }
  }

  def responseTryResult[T](record: Try[T])(implicit conv: OFormat[T]): Result = {
    record match {
      case Success(quote)     => Ok(Json.toJson(quote))
      case Failure(exception) => internalServerError(exception.getMessage)
    }
  }

  def responseEitherResult[T](record: Either[ResponseMsg, T])(implicit conv: OFormat[T]): Result = {
    record match {
      case Left(errorMsg) =>
        errorMsg match {
          case EmptyDbMsg               => notFound(EmptyDbMsg.msg)
          case invalidDate: InvalidDate => badRequest(invalidDate.msg)
        }
      case Right(quote) => Ok(Json.toJson(quote))
    }
  }

  def responseEsResult[T](response: Response[T]): Result = {
    if (response.isSuccess) {
      Ok(s"Success: ${response.body}")
    } else {
      log.error(s"Error while deleting an index: ${response.error.reason}")
      notFound(s"${response.error.reason}")
    }
  }

}
