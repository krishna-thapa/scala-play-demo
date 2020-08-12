package daos

import slick.ast.ScalaBaseType.doubleType
import slick.lifted.{ Rep, SimpleFunction }

abstract class CommonMethods[T] {

  //type T  //https://stackoverflow.com/questions/1154571/scala-abstract-types-vs-generics

  def listAllQuotes(): Seq[T]

  def listSelectedQuote(id: Int): Option[T] = None

  def listRandomQuote(records: Int): Seq[T] = Seq.empty

  def randomFunction: Rep[Double] = SimpleFunction.nullary[Double]("random")

}
