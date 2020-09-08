package response

import play.api.libs.json.OFormat
import play.api.mvc.Result

import scala.util.Try

trait ResultResponse {

  def responseSeqResult[T](records: Seq[T])(implicit conv: OFormat[T]): Result

  def responseOptionResult[T](record: Option[T])(implicit conv: OFormat[T]): Result

  def responseTryResult[T](record: Try[T])(implicit conv: OFormat[T]): Result
}
