package com.krishna.table

import com.krishna.model.base.QuoteResource
import slick.jdbc.PostgresProfile.api._

trait TableId[T <: QuoteResource] {
  this: Table[T] =>

  def id: Rep[Int]       = column[Int]("id", O.AutoInc)
  def csvId: Rep[String] = column[String]("csv_id")
}
