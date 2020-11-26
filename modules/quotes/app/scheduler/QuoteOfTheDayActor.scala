package scheduler

import java.util.Date

import akka.actor.{ Actor, Props }
import cache.CacheService
import com.krishna.util.DateConversion.{ dateFormatter, now }
import com.krishna.util.Logging
import javax.inject.Inject

object QuoteOfTheDayActor {
  def props: Props = Props[QuoteOfTheDayActor]()

  case class GetQuoteOfTheDay(date: Date)
}

//Implement task by extends Actor
class QuoteOfTheDayActor @Inject()(cacheService: CacheService) extends Actor with Logging {

  import QuoteOfTheDayActor._

  override def receive: Receive = {
    case GetQuoteOfTheDay(date: Date) =>
      val foo = cacheService.cacheQuoteOfTheDay(dateFormatter(date))
      println(foo)
  }
}
