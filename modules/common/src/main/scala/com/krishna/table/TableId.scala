package com.krishna.table

import com.krishna.model.base.IdResource
import slick.jdbc.PostgresProfile.api._

trait TableId[T <: IdResource] {
  this: Table[T] =>

  def id: Rep[Int] = column[Int]("id", O.AutoInc)
}
