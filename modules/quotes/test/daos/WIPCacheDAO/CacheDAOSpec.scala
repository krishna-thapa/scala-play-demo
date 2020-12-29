package daos.WIPCacheDAO

import akka.actor.ActorSystem
import com.dimafeng.testcontainers.{ ForAllTestContainer, GenericContainer, MultipleContainers }
import helper.testContainers.PostgresInstance
import org.scalatest.matchers.should.Matchers

class CacheDAOSpec
    extends PostgresInstance
    //with RedisCacheInstance
    with ForAllTestContainer
    with Matchers {

  val system: ActorSystem = ActorSystem()

  // Initialize the QuoteQueryDao class
  //val quoteQueryDao: QuoteQueryDAO = Application.instanceCache[QuoteQueryDAO].apply(app)

  val containerRedis: GenericContainer =
    GenericContainer("redis:alpine", exposedPorts = Seq(6379))

  // Need to test more than one container in your test, you could use MultipleContainers
  override val container: MultipleContainers = MultipleContainers(containerPostgres, containerRedis)

  // Load the quotes test sql queries in the test Postgres docker container
  loadQueries("quoteTestQueries")

  //val c: CacheApi = mock[CacheApi]

  //val cacheDAO: CacheDAO = new CacheDAO(c, quote)
  container.start()

  behavior of "CacheDAO"
  it should "return random quote and cache in Redis database" in {
    //val quote = cacheDAO.foo()
    //quote.isRight shouldBe true
  }

}
