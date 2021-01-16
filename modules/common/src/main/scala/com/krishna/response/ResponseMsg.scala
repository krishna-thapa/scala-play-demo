package com.krishna.response

// Don't need to match every case classes so no need of sealed
trait ResponseMsg {
  val msg: String
}

object ResponseMsg {
  //TODO add more for auth and search modules
  case object EmptyDbMsg extends ResponseMsg {
    override val msg: String = "Database is empty!"
  }

  case class InvalidDate(date: String) extends ResponseMsg {
    override val msg: String = s"Date has to be within last 5 days. Invalid date: $date"
  }

  case class InvalidCsvId(csvId: String) extends ResponseMsg {
    override val msg: String = s"Id of quote should be in CSV123 format!. Invalid id: $csvId"
  }
}
