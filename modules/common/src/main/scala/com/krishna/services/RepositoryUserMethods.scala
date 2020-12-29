package com.krishna.services

import com.krishna.model.base.IdResource
import com.krishna.table.TableWithUserId
import slick.dbio.{ DBIOAction, Effect, NoStream, Streaming }
import slick.jdbc.PostgresProfile.api._

trait RepositoryUserMethods[T <: IdResource, QuoteTable <: Table[T] with TableWithUserId[T]]
    extends CommonMethods[T, QuoteTable] {

  private val getRecordForUser: (Int, Int) => Query[QuoteTable, T, Seq] = (id: Int, userId: Int) =>
    tables.filter(record => record.id === id && record.userId === userId)

  def getAllQuotesForUser(userId: Int): DBIOAction[Seq[T], Streaming[T], Effect.Read] = {
    tables.filter(_.userId === userId).sortBy(_.id).result
  }

  def getSelectedQuote(id: Int, userId: Int): DBIOAction[Option[T], NoStream, Effect.Read] = {
    getRecordForUser(id, userId).result.headOption
  }

  def deleteCustomQuote(id: Int, userId: Int): DBIOAction[Int, NoStream, Effect.Write] = {
    getRecordForUser(id, userId).delete
  }

  def getRandomRecords(records: Int, userId: Int): DBIOAction[Seq[T], Streaming[T], Effect.Read] = {
    tables
      .filter(_.userId === userId)
      .sortBy(_ => randomFunction)
      .take(records)
      .result
  }
}
