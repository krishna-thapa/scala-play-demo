package helper.testContainers

import akka.actor.ActorSystem
import helper.GenericFlatSpec
import play.api.cache.redis.CacheApi
import play.api.inject.guice.{ GuiceApplicationBuilder, GuiceInjectorBuilder }

trait RedisCacheInstance extends GenericFlatSpec {
  val system: ActorSystem = GuiceApplicationBuilder().build().actorSystem
  //implicit val actorSystem: ActorSystem = mock[ActorSystem]

//  implicit override lazy val app: Application =
//    new GuiceApplicationBuilder()
//      .bindings(bind[ActorSystem].to(system))
//      //.overrides(bind[ActorSystem].to[system])
//      .configure(
//        Configuration(
//          "play.modules.enabled"  -> List("play.api.cache.redis.RedisCacheModule"),
//          "play.modules.disabled" -> Seq("play.api.cache.EhCacheModule")
//        )
//      )
//      //.bindings(inject.bind[ActorSystem].to(system))
//      .build()

  //private val system = GuiceApplicationBuilder().build().actorSystem
  val injector = new GuiceInjectorBuilder()
    .configure("play.modules.enabled" -> ("play.api.cache.redis.RedisCacheModule"))
    .injector

  val cacheApi = injector.instanceOf[CacheApi]

  // Initialize the CacheApi trait
  //val cacheApi: CacheApi = Application.instanceCache[CacheApi].apply(app)
}
