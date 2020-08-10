package daos

import slick.ast.ScalaBaseType.doubleType
import slick.lifted.{ Rep, SimpleFunction }

trait CommonMethods {

  type T

  def listAllQuotes(): Seq[T]

  def listSelectedQuote(id: Int): Option[T] = None

  def listRandomQuote(records: Int): Seq[T] = Seq.empty

  def randomFunction: Rep[Double] = SimpleFunction.nullary[Double]("random")

}
