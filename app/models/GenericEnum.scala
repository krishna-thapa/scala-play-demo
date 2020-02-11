package models

import org.slf4j.LoggerFactory
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue}
import play.api.mvc.PathBindable

abstract class GenericEnum extends Enumeration {

  private val logger = LoggerFactory.getLogger(classOf[GenericEnum])

  // Bind an enum to a play framework form:
  implicit def EnumFormatter: Formatter[Value] = new Formatter[Value] {
    override val format = Some(("format.enum", Nil))

    override def bind(
        key: String,
        data: Map[String, String]
    ): Either[Seq[FormError], Value] = {
      val enumValue: String = data.get(key).head
      try {
        Right(GenericEnum.this.withName(enumValue))
      } catch {
        case e: NoSuchElementException =>
          logger.error(s"Invalid Enum type: $enumValue")
          Left(Seq(FormError(key, s"Invalid Enum type: $enumValue}")))
      }
    }

    override def unbind(key: String, value: Value): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  // Bind the custom enum to JSON formatter
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
        case None =>
          logger.error(s"Invalid Enum type: $value")
          Left(s"Invalid Enum type: $value")
      }

    override def unbind(key: String, value: Value): String = value.toString
  }
}
