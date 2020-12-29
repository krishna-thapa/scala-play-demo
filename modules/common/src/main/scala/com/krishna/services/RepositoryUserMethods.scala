package com.krishna.services

import com.krishna.model.base.IdResource
import com.krishna.table.TableWithUserId
import slick.dbio.{ DBIOAction, Effect, NoStream, Streaming }
import slick.jdbc.PostgresProfile.api._

trait RepositoryUserMethods[T <: IdResource, QuoteTable <: Table[T] with TableWithUserId[T]]
    extends CommonMethods[T, QuoteTable] {

  def getAllQuotesForUser(userId: Int): DBIOAction[Seq[T], Streaming[T], Effect.Read] = {
    tables.filter(_.userId === userId).sortBy(_.id).result
  }

  def getSelectedQuote(id: Int): DBIOAction[Option[T], NoStream, Effect.Read] = {
    tables.filter(_.id === id).result.headOption
  }

  def deleteCustomQuote(id: Int): DBIOAction[Int, NoStream, Effect.Write] = {
    tables.filter(_.id === id).delete
  }
}
