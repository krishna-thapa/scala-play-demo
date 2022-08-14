package com.krishna.table

import com.krishna.model.base.WithUserIdResource
import slick.jdbc.PostgresProfile.api._

import java.util.UUID

trait TableWithUserId[T <: WithUserIdResource] extends TableId[T] {
  this: Table[T] =>

  def userId: Rep[UUID] = column[UUID]("user_id")
}
