package utils

import java.text.SimpleDateFormat
import java.util.{ Calendar, Date }

import scala.util.{ Failure, Success, Try }

object DateConversion extends Logging {

  val now: Date                     = Calendar.getInstance().getTime
  val dateFormatter: Date => String = value => new SimpleDateFormat("yyyy-MM-dd").format(value)

  def getCurrentDate: String = dateFormatter(now)

  /**
    * @param strDate: Takes the string date in milliseconds format
    * @return converts to simple yyyy-MM-dd format if no exception,
    *         if exception then it returns current date
    */
  def convertToDate(strDate: String): String = {
    val date = Try(new Date(strDate.toLong))
    date match {
      case Failure(ex) =>
        log.warn("Failure in the date conversion: " + ex.getMessage)
        dateFormatter(now)
      case Success(value) => dateFormatter(value)
    }
  }
}
