package dao

import com.dimafeng.testcontainers.ElasticsearchContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.krishna.model.{ Genre, QuotesQuery }
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.playjson.playJsonHitReader
import com.sksamuel.elastic4s.requests.bulk.BulkResponse
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import daos.QuoteQueryDAO
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Configuration

import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class SearchEsDAOSpec extends AnyFlatSpec with TestContainerForAll {

  override val containerDef: ElasticsearchContainer.Def =
    ElasticsearchContainer.Def("docker.elastic.co/elasticsearch/elasticsearch:7.10.1")

  val container: ElasticsearchContainer = startContainers()

  val getHostAddress: Array[String] = container.httpHostAddress.split(":")

  val mockConfig: Configuration = Configuration.from(
    Map(
      "elasticsearch.ESHOST"      -> s"${getHostAddress.head}",
      "elasticsearch.ESPORT"      -> s"${getHostAddress(1)}",
      "elasticsearch.ESINDEXNAME" -> "test"
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

  behavior of "SearchInEsDAO"
  it should "return check indication if the index exists" in {
    searchDao.doesIndexExists shouldBe false
  }

  it should "store 2 mock quotes in ES" in {
    when(mockQuoteQueryDAO.listRandomQuote(2)).thenReturn(mockQuotes)

    val result: Response[BulkResponse] =
      Await.result(searchDao.getAndStoreQuotes(2), Duration.Inf)
    val indexIds: Seq[String] = result.map(_.items).result.map(_.id)

    result.isSuccess shouldBe true
    indexIds shouldBe Seq("csv101", "csv102")

    searchDao.countDocsInIndex shouldBe 2
  }

  it should "perform search query in elastic search" in {
    val getMatchedQuote: Future[Response[SearchResponse]] = searchDao.searchQuote("Test quote 2")
    val matchedQuote: SearchResponse                      = Await.result(getMatchedQuote, Duration.Inf).result

    matchedQuote.ids shouldBe Seq("csv102")
    matchedQuote.to[QuotesQuery].toList shouldBe mockQuotes.tail
  }

  it should "perform search on all matched docs" in {
    val getMatchedQuote: Future[Response[SearchResponse]] = searchDao.searchQuote("quote")
    val matchedQuote: SearchResponse                      = Await.result(getMatchedQuote, Duration.Inf).result

    matchedQuote.to[QuotesQuery].toList.sortBy(_.id) shouldBe mockQuotes
  }

  it should "searched text case should not matter" in {
    val getMatchedQuote: Future[Response[SearchResponse]] = searchDao.searchQuote("QuOt")
    val matchedQuote: SearchResponse                      = Await.result(getMatchedQuote, Duration.Inf).result

    matchedQuote.to[QuotesQuery].toList.sortBy(_.id) shouldBe mockQuotes
  }

  it should "return empty if not found" in {
    val getMatchedQuote: Future[Response[SearchResponse]] = searchDao.searchQuote("wrong text")
    val matchedQuote: SearchResponse                      = Await.result(getMatchedQuote, Duration.Inf).result

    matchedQuote.to[QuotesQuery].toList shouldBe Seq.empty
  }

  it should "return result with correct limit" in {
    val getMatchedQuote: Future[Response[SearchResponse]] =
      searchDao.searchQuote("quote", limit = 1)
    val matchedQuote: SearchResponse = Await.result(getMatchedQuote, Duration.Inf).result
    matchedQuote.hits.size shouldBe 1
  }

  it should "return result with correct offset" in {
    val getMatchedQuote: Future[Response[SearchResponse]] =
      searchDao.searchQuote("Test", offset = 1)
    val matchedQuote: SearchResponse = Await.result(getMatchedQuote, Duration.Inf).result
    matchedQuote.hits.size shouldBe 1
  }

  it should "delete the index in ES" in {
    val isDeleted: Boolean =
      Await
        .result(searchDao.deleteQuotesIndex(searchDao.indexName), Duration.Inf)
        .result
        .acknowledged
    isDeleted shouldBe true
  }
}
