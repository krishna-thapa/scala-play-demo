package dao

import com.krishna.model.{ Genre, QuotesQuery }
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import com.whisk.docker.impl.dockerjava.DockerKitDockerJava
import com.whisk.docker.scalatest.DockerTestKit
import daos.QuoteQueryDAO
import helper.DockerElasticsearchService
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.time._
import play.api.Configuration

import scala.concurrent.duration.{ Duration, DurationInt, FiniteDuration }
import scala.concurrent.{ Await, Future }

class SearchInEsDAOSpec
    extends AnyFlatSpec
    with Matchers
    with DockerElasticsearchService
    with DockerTestKit
    with DockerKitDockerJava {

  override val StartContainersTimeout: FiniteDuration = 5.minutes

  implicit val pc: PatienceConfig = PatienceConfig(Span(20, Seconds), Span(1, Second))

  val mockConfig: Configuration = Configuration.from(
    Map(
      "ES.ES_HOST"       -> "localhost",
      "ES.ES_PORT"       -> "2237",
      "ES.ES_INDEX_NAME" -> "test"
    )
  )

  val mockQuoteQueryDAO: QuoteQueryDAO = mock[QuoteQueryDAO]
  val searchDao: SearchInEsDAO         = new SearchInEsDAO(mockQuoteQueryDAO, mockConfig)

  val baseQuote: QuotesQuery = QuotesQuery(
    id = 1,
    csvId = "csv101",
    quote = "Test quote 1",
    author = None,
    genre = Some(Genre.age)
  )
  val mockQuotes: Seq[QuotesQuery] =
    Seq(baseQuote, baseQuote.copy(id = 2, csvId = "csv102", quote = "Test quote 2"))

  "elasticsearch container" should "be ready" in {
    isContainerReady(elasticsearchContainer).futureValue shouldBe true
    elasticsearchContainer.getPorts().futureValue.get(9300) should not be empty
    elasticsearchContainer.getIpAddresses().futureValue should not be Seq.empty
  }

  "doesIndexExists" should "return false before index creation" in {
    searchDao.doesIndexExists shouldBe false
  }

  "getAndStoreQuotes" should "store 2 mock quotes in ES" in {
    when(mockQuoteQueryDAO.listRandomQuote(2)).thenReturn(mockQuotes)
    val result: Seq[Response[IndexResponse]] =
      Await.result(Future.sequence(searchDao.getAndStoreQuotes(2)), Duration.Inf)
    val indexIds: Seq[String] = result.map(_.result).map(_.id)

    result.head.isSuccess shouldBe true
    indexIds shouldBe Seq("csv101", "csv102")
  }

}
