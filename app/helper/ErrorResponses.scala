package helper

import play.api.mvc.Result

trait ErrorResponses {

  def badRequest(msg: String): Result

  def notFound(msg: String): Result

  def internalServerError(msg: String): Result

}
