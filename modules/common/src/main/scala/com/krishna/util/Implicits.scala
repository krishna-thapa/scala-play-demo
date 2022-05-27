package com.krishna.util

import com.krishna.model.Genre
import com.krishna.model.Genre.Genre
import slick.ast.BaseTypedType
import slick.jdbc.PostgresProfile.api._

object Implicits extends Enumeration {

  // Slick mapping custom type enum in database column
  implicit val genreEnumMapper: BaseTypedType[Genre.Value] =
    MappedColumnType.base[Genre, String](
      { genre =>
        genre.toString
      },
      { string =>
        Genre.withName(string)
      }
    )

}
