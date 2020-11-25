package scheduler

import java.util.{ Calendar, Date }

import akka.actor.{ ActorRef, ActorSystem, Props }
import com.google.inject.Inject
import com.typesafe.akka.`extension`.quartz.QuartzSchedulerExtension
import play.api.inject.ApplicationLifecycle
import play.inject.Injector

import scala.concurrent.Future

class ApplicationStart @Inject()(
    lifecycle: ApplicationLifecycle,
    system: ActorSystem,
    injector: Injector
) {
  val name = "ApplicationStart"

  // Shut-down hook
  lifecycle.addStopHook { () =>
    Future.successful()
  }

  val now: Date = Calendar.getInstance().getTime

  // Start scheduling
  val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(system)
  val receiver: ActorRef =
    system.actorOf(Props.create(classOf[GuiceActorProducer], injector, classOf[QuoteOfTheDayActor]))

  scheduler.schedule("every5seconds", receiver, QuoteOfTheDayActor.GetQuoteOfTheDay(now), None)

}
