package com.krishna.util

import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError

import scala.concurrent.{ ExecutionContext, Future }

object UtilImplicits extends Logging {

  // An implicit class to apply future recover method for any time of Future[Result] response
  implicit class ErrorRecover(futureResult: Future[Result])(implicit
    executionContext: ExecutionContext
  ) {

    def errorRecover: Future[Result] = {
      futureResult.recover { case e =>
        log.error(s"Internal Error on responding future of result method: ${ e.getMessage }")
        InternalServerError(e.getMessage)
      }
    }

  }

  // An implicit class to apply for Result to return future of Result
  implicit class ToFuture(result: Result) {

    def toFuture: Future[Result] = {
      Future.successful(result)
    }

  }

  // An implicit class to apply for validating email address on String
  implicit class ValidEmail(email: String) {

    def validEmail: Boolean = """^[A-Za-z0-9+_.-]+@(.+)$""".r.matches(email)

  }

}
