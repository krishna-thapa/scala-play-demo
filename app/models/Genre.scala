package models

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue, Json, OFormat}

object Genre extends Enumeration {

  type Genre = Value

  val age, alone, best, birthday, dad, dreams, amazing, art, good, anger,
  happiness, anniversary, architecture, home, attitude, hope, beauty,
  humor, life, love, patience, business, car, poetry, politics, time,
  trust, change, war, women, work, communication, computers, cool,
  courage, dating, death, design, education, diet, experience, faith,
  famous, environmental, equality, failure, fear, forgiveness, family,
  freedom, friendship, finance, fitness, food, funny, future, gardening,
  god, government, great, graduation, history, health, imagination, inspirational,
  intelligence, jealousy, knowledge, leadership, learning, legal, marriage, men,
  medical, mom, money, morning, motivational, movies, music, movingon, nature,
  parenting, patriotism, peace, pet, positive, power, religion, respect, relationship,
  science, smile, success, romantic, sad, teacher, society, teen, thankful, sports,
  strength, sympathy, technology, travel, truth, wisdom, wedding, christmas, easter,
  fathersday, memorialday, mothersday, newyears, saintpatricksday, thanksgiving,
  valentinesday = Value

  // Bind an enum to a play framework form:
  // https://github.com/jethrogillgren/play-samples/blob/workingversion/play-scala-hello-world-tutorial/app/models/Search.scala

  implicit object GenreFormatter extends Formatter[Genre.Value] {
    override val format = Some(("format.enum", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], models.Genre.Value] = {
      try {
        Right(Genre.withName(data.get(key).head))
      } catch {
        case e: NoSuchElementException => Left(Seq(FormError(key, "Invalid Genre type")))
      }
    }

    override def unbind(key: String, value: models.Genre.Value): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  implicit  val genreFormat: Format[Genre] = new Format[Genre.Genre] {
    override def reads(json: JsValue): JsResult[Genre] = {
      JsSuccess(Genre.withName(json.as[String]))
    }

    override def writes(genreEnum: Genre.Genre): JsValue = {
      JsString(genreEnum.toString)
    }
  }
}
