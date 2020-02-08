package models

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue}
import play.api.mvc.PathBindable

abstract class GenericEnum extends Enumeration {

  // Bind an enum to a play framework form:
  // https://github.com/jethrogillgren/play-samples/blob/workingversion/play-scala-hello-world-tutorial/app/models/Search.scala
  implicit def EnumFormatter: Formatter[Value] = new Formatter[Value] {
    override val format = Some(("format.enum", Nil))

    override def bind(
        key: String,
        data: Map[String, String]
    ): Either[Seq[FormError], Value] = {
      try {
        Right(GenericEnum.this.withName(data.get(key).head))
      } catch {
        case e: NoSuchElementException =>
          Left(Seq(FormError(key, "Invalid Enum type")))
      }
    }

    override def unbind(key: String, value: Value): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  implicit val enumFormat: Format[Value] = new Format[Value] {
    override def reads(json: JsValue): JsResult[Value] = {
      JsSuccess(GenericEnum.this.withName(json.as[String]))
    }

    override def writes(enum: Value): JsValue = {
      JsString(enum.toString)
    }
  }

  // url path binding (routes) using enum
  implicit def bindableEnum: PathBindable[Value] = new PathBindable[Value] {
    override def bind(key: String, value: String): Either[String, Value] =
      GenericEnum.this.values
        .find(_.toString.toLowerCase == value.toLowerCase) match {
        case Some(v) => Right(v)
        case None    => Left("Unknown enum type '" + value + "'")
      }

    override def unbind(key: String, value: Value): String = value.toString
  }
}
