package helper

import play.api.libs.json.OFormat
import play.api.mvc.Result

trait ErrorResponses {

  def badRequest(msg: String): Result

  def notFound(msg: String): Result

  def internalServerError(msg: String): Result

  def responseSeqResult[T](records: Seq[T])(implicit conv: OFormat[T]): Result

}
