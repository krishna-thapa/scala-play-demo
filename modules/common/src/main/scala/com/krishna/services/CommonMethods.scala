package com.krishna.services

import slick.dbio.{ DBIOAction, Effect, Streaming }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ Rep, SimpleFunction, TableQuery }

trait CommonMethods[T, QuoteTable <: Table[T]] {

  def tables: TableQuery[QuoteTable]

  /* Random build in function in Postgres table*/
  def randomFunction: Rep[Double] = SimpleFunction.nullary[Double]("random")

  /*
   * Defined custom function for slick 3
   * aware that "random" function is database specific
   */
  def getRandomRecords(records: Int): DBIOAction[Seq[T], Streaming[T], Effect.Read] = {
    tables
      .sortBy(_ => randomFunction)
      .take(records)
      .result
  }

}
