package daos

import akka.actor.ActorSystem
import helper.testContainers.RedisCacheInstance
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import redis.RedisClient

class CacheDAOSpec extends RedisCacheInstance with BeforeAndAfterAll {

  implicit val sys: ActorSystem = ActorSystem("test")

  var redis: RedisClient = null

  val mockQuotesDAO: QuoteQueryDAO = mock[QuoteQueryDAO]
  val cacheDAO: CacheDAO           = new CacheDAO(cacheApi, mockQuotesDAO)

  override def beforeAll(): Unit = {
    redis = RedisClient() // Connect to localhost/127.0.0.1:6379
  }

  behavior of "CacheDAO"
  it should "return empty if the are no fav quotes for a user id" in {
    redis.ping().futureValue shouldBe "PONG"
  }

  it should "rsdeturn empty if the are no fav quotes for a user id" in {
    cacheDAO.testCache shouldBe "PONG"
    //redis.ping().futureValue shouldBe "PONG"
  }

  override def afterAll(): Unit = {
    println("skladjbfnlas")
    redis.stop()
  }
}
