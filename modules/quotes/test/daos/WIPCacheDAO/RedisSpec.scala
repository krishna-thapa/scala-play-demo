package daos.WIPCacheDAO

import com.dimafeng.testcontainers.{ Container, ForAllTestContainer, GenericContainer }
import daos.CacheDAO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{ Application, Configuration }

class RedisSpec extends AnyFlatSpec with ForAllTestContainer with GuiceOneAppPerSuite {

  implicit val systemq = GuiceApplicationBuilder().build().actorSystem
  //implicit val actorSystem: ActorSystem = mock[ActorSystem]

  override def container: Container =
    GenericContainer("redis:5.0.3-alpine", exposedPorts = Seq(6379))

  implicit override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        Configuration(
          "play.modules.enabled"  -> Seq("play.api.cache.redis.RedisCacheModule"),
          "play.modules.disabled" -> Seq("play.api.cache.EhCacheModule")
        )
      )
      //.overrides(bind[ActorSystem].to(actorSystem))
      .build()

  val cacheApi: CacheDAO = Application.instanceCache[CacheDAO].apply(app)

  "GenericContainer" should "start nginx and expose 80 port" in {
    container.start()
  }
}
