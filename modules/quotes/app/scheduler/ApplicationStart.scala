package scheduler

import com.google.inject.Inject
import com.krishna.util.Logging
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

class ApplicationStart @Inject() (
  lifecycle: ApplicationLifecycle
//    system: ActorSystem,
  //    injector: Injector
) extends Logging {

  // Shut-down hook
  lifecycle.addStopHook { () =>
    Future.successful((): Unit)
  }

  // TODO: Start scheduling
  // val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(system)
  // val receiver: ActorRef = system.actorOf(Props.create(classOf[GuiceActorProducer], injector, classOf[QuoteOfTheDayActor]))

  // scheduler.schedule("every5seconds", receiver, QuoteOfTheDayActor.GetQuoteOfTheDay(now), None)

}
