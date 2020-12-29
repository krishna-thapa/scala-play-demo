package com.krishna.table

import com.krishna.model.base.IdResource
import slick.jdbc.PostgresProfile.api._

trait TableWithUserId[T <: IdResource] extends TableId[T] {
  this: Table[T] =>

  def userId: Rep[Int] = column[Int]("user_id")
}
