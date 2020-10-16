package response

import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{ Json, OFormat }
import play.api.mvc.Result
import play.api.mvc.Results.{ NotAcceptable, _ }
import response.ResponseMsg._
import utils.Logging

import scala.util.{ Failure, Success, Try }

trait ResponseMethod extends ErrorResponses with ResultResponse with Logging {

  def badRequest(msg: String): Result = {
    log.error(s"Bad request error: $msg")
    BadRequest(Json.toJson(ResponseErrorMsg(msg)))
  }

  def notFound(msg: String): Result = {
    log.warn(s"Not Found error: $msg")
    NotFound(Json.toJson(ResponseErrorMsg(msg)))
  }

  def internalServerError(msg: String): Result = {
    log.error(s"Internal server error: $msg")
    InternalServerError(Json.toJson(ResponseErrorMsg(msg)))
  }

  def unauthorized(msg: String): Result = {
    val errorMessage: String = s"Wrong password for user: $msg"
    log.error(errorMessage)
    Unauthorized(Json.toJson(ResponseErrorMsg(errorMessage)))
  }

  def notAcceptable(msg: String): Result = {
    val errorMessage: String = s"Account already exist with: $msg"
    log.error(errorMessage)
    NotAcceptable(Json.toJson(ResponseErrorMsg(errorMessage)))
  }

  def bcryptValidationFailed(msg: String): Result = {
    val errorMessage: String = s"Bcrypt encryption failed: $msg"
    log.error(errorMessage)
    FailedDependency(Json.toJson(ResponseErrorMsg(errorMessage)))
  }

  // TODO: Might need to separate in different trait
  def responseSeqResult[T](records: Seq[T])(implicit conv: OFormat[T]): Result = {
    if (records.nonEmpty) Ok(Json.toJson(records))
    else notFound(EmptyDbMsg.msg)
  }

  def responseWithSeq(records: Seq[String]): Result = {
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

}
