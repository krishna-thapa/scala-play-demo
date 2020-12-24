package com.krishna.services

import com.krishna.model.base.QuoteResource
import com.krishna.table.TableId
import slick.ast.ScalaBaseType.doubleType
import slick.dbio.{ DBIOAction, Effect, NoStream, Streaming }
import slick.lifted.{ Rep, SimpleFunction, TableQuery }
import slick.jdbc.PostgresProfile.api._

trait RepositoryMethods[T <: QuoteResource, QuoteTable <: Table[T] with TableId[T]] {

  //type T  //https://stackoverflow.com/questions/1154571/scala-abstract-types-vs-generics

  def tables: TableQuery[QuoteTable]

  def getAllQuotes: DBIOAction[Seq[T], Streaming[T], Effect.Read] = {
    tables.sortBy(_.id).result
  }

//  def getRandomQuote(records: Int): FixedSqlStreamingAction[Seq[T], T, Effect.Read] = {
//    tables
//      .sortBy(_ => randomFunction)
//      .take(records)
//      .result
//
//  }

  def getSelectedQuote(id: Int): DBIOAction[Option[T], NoStream, Effect.Read] = {
    tables.filter(_.id === id).result.headOption
  }

  def deleteCustomQuote(id: Int): DBIOAction[Int, NoStream, Effect.Write] = {
    tables.filter(_.id === id).delete
  }

  //val rand = SimpleFunction.nullary[Double]("random")
}
