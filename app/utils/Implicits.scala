package utils

import models.Genre
import models.Genre.Genre
import slick.ast.BaseTypedType
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

object Implicits extends Enumeration {

  // Slick mapping custom type enum in database column
  implicit val genreEnumMapper: BaseTypedType[Genre.Value] =
    MappedColumnType.base[Genre, String]({ genre =>
      genre.toString
    }, { string =>
      Genre.withName(string)
    })

  // return result from future
  implicit class FutureResult[T](future: Future[T]) {
    def andGetResult(timeout: Option[Int] = None): T =
      Await.result(future, timeout.getOrElse(60).seconds)
  }
}
