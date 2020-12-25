package com.krishna.table

import com.krishna.model.base.WithCSCVIdResource
import slick.jdbc.PostgresProfile.api._

trait TableWithCSVId[T <: WithCSCVIdResource] extends TableId[T] {
  this: Table[T] =>

  def csvId: Rep[String] = column[String]("csv_id")
}
