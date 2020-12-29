package com.krishna.services

import com.krishna.model.base.IdResource
import com.krishna.table.TableId
import slick.dbio.{ DBIOAction, Effect, Streaming }
import slick.jdbc.PostgresProfile.api._

trait RepositoryQuoteMethods[T <: IdResource, QuoteTable <: Table[T] with TableId[T]]
    extends CommonMethods[T, QuoteTable] {

  def getAllQuotes: DBIOAction[Seq[T], Streaming[T], Effect.Read] = {
    tables.sortBy(_.id).result
  }

}
