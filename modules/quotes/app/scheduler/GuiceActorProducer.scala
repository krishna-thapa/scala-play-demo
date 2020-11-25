package scheduler

import akka.actor.{ Actor, IndirectActorProducer }

class GuiceActorProducer(val injector: play.inject.Injector, val cls: Class[_ <: Actor])
    extends IndirectActorProducer {

  override def actorClass: Class[Actor] = classOf[Actor]

  override def produce(): Actor = {
    injector.instanceOf(cls)
  }

}
