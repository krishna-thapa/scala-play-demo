package daos.WIPCacheDAO

import daos.CacheDAO
import helper.GenericFlatSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{ Application, Configuration }

trait RedisCacheInstance extends GenericFlatSpec with GuiceOneAppPerSuite {

  val systemq = GuiceApplicationBuilder().build().actorSystem
  //implicit val actorSystem: ActorSystem = mock[ActorSystem]

  implicit override lazy val app: Application =
    new GuiceApplicationBuilder()
    //.bindings(bind[ActorSystem].to(systemq))
      .configure(
        Configuration(
          "play.modules.enabled"  -> Seq("play.api.cache.redis.RedisCacheModule"),
          "play.modules.disabled" -> Seq("play.api.cache.EhCacheModule")
        )
      )
      //.bindings(bind[ActorSystem].to(systemq))
      .build()

  //private val system = GuiceApplicationBuilder().build().actorSystem

  // Initialize the CacheApi trait
  val cacheApi: CacheDAO = Application.instanceCache[CacheDAO].apply(app)
}
