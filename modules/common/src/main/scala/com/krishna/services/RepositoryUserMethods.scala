package com.krishna.services

import com.krishna.model.base.WithUserIdResource
import com.krishna.table.TableWithUserId
import slick.dbio.{ DBIOAction, Effect, NoStream, Streaming }
import slick.jdbc.PostgresProfile.api._

import java.util.UUID

trait RepositoryUserMethods[T <: WithUserIdResource, QuoteTable <: Table[T] with TableWithUserId[T]]
    extends CommonMethods[T, QuoteTable] {

  private val getRecordForUser: (Int, UUID) => Query[QuoteTable, T, Seq] =
    (id: Int, userId: UUID) => tables.filter(record => record.id === id && record.userId === userId)

  def getAllQuotesForUser(userId: UUID): DBIOAction[Seq[T], Streaming[T], Effect.Read] = {
    tables.filter(_.userId === userId).sortBy(_.id).result
  }

  def getSelectedQuote(id: Int, userId: UUID): DBIOAction[Option[T], NoStream, Effect.Read] = {
    getRecordForUser(id, userId).result.headOption
  }

  def deleteCustomQuote(id: Int, userId: UUID): DBIOAction[Int, NoStream, Effect.Write] = {
    getRecordForUser(id, userId).delete
  }

  def getRandomRecords(
    records: Int,
    userId: UUID
  ): DBIOAction[Seq[T], Streaming[T], Effect.Read] = {
    tables
      .filter(_.userId === userId)
      .sortBy(_ => randomFunction)
      .take(records)
      .result
  }

}
