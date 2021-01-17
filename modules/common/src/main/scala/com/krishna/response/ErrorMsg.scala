package com.krishna.response

// Don't need to match every case classes so no need of sealed
trait ErrorMsg {
  val msg: String
}

object ErrorMsg {
  //TODO add more from auth and search modules
  case object EmptyDbMsg extends ErrorMsg {
    override val msg: String = "Database is empty!"
  }

  case class InvalidDate(date: String) extends ErrorMsg {
    override val msg: String = s"Date has to be within last 5 days. Invalid date: $date"
  }

  case class InvalidCsvId(csvId: String) extends ErrorMsg {
    override val msg: String = s"Id of quote should be in CSV123 format!. Invalid id: $csvId"
  }

  case object NoAuthorizationField extends ErrorMsg {
    override val msg: String = "No Authorization field found in request header!"
  }

  case class TokenDecodeFailure(exception: String) extends ErrorMsg {
    override val msg: String = s"JWT token decode failed with an error : $exception"
  }

  case object AuthenticationFailed extends ErrorMsg {
    override val msg: String = "Authentication failed: Do not have access!"
  }

  case class AuthorizationForbidden(email: String) extends ErrorMsg {
    override val msg: String = s"Forbidden authorization for user: $email"
  }
}
