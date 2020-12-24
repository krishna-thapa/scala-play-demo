package com.krishna.services

import slick.ast.ScalaBaseType.doubleType
import slick.lifted.{ Rep, SimpleFunction }

trait CommonMethods[T] {

  //type T  //https://stackoverflow.com/questions/1154571/scala-abstract-types-vs-generics

  def listAllQuotes(): Seq[T]

  def listSelectedQuote(id: Int): Option[T] = None

  def listRandomQuote(records: Int): Seq[T]

  def randomFunction: Rep[Double] = SimpleFunction.nullary[Double]("random")

}
