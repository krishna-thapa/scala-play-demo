package dao

import com.dimafeng.testcontainers.ElasticsearchContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.krishna.model.{ Genre, QuotesQuery }
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.playjson.playJsonHitReader
import com.sksamuel.elastic4s.requests.bulk.BulkResponse
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import config.CompletionCustomSuggestion
import daos.QuoteQueryDAO
import models.{ AuthorSearchResponse, QuoteWithAuthor }
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

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
    author = Some("author1 Test"),
    genre = Some(Genre.age)
  )
  val mockQuotes: Seq[QuotesQuery] =
    Seq(
      baseQuote,
      baseQuote
        .copy(id = 2, csvId = "csv102", author = Some("author2 Test"), quote = "Test quote 2"),
      baseQuote
        .copy(id = 3, csvId = "csv103", author = Some("random author"), quote = "Random quote")
    )

  behavior of "SearchInEsDAO"
  it should "return check indication if the index exists" in {
    searchDao.doesIndexExists shouldBe false
  }

  it should "store 2 mock quotes in ES" in {
    when(mockQuoteQueryDAO.listRandomQuote(3)).thenReturn(mockQuotes)

    val result: Response[BulkResponse] =
      Await.result(searchDao.getAndStoreQuotes(3), Duration.Inf)
    val indexIds: Seq[String] = result.map(_.items).result.map(_.id)

    result.isSuccess shouldBe true
    indexIds shouldBe Seq("csv101", "csv102", "csv103")

    searchDao.countDocsInIndex shouldBe 3
  }

  it should "perform search query in elastic search using match prefix API" in {
    val getMatchedQuote: Future[Response[SearchResponse]] = searchDao.searchQuote("Test quote 2")
    val matchedQuote: SearchResponse                      = Await.result(getMatchedQuote, Duration.Inf).result

    matchedQuote.ids shouldBe Seq("csv102")
    val expected: QuoteWithAuthor = QuoteWithAuthor(mockQuotes.tail.head, None)
    matchedQuote.to[QuoteWithAuthor].head shouldBe expected
  }

  it should "perform search query using match query if match prefix return nil" in {
    val getMatchedQuote: Future[Response[SearchResponse]] = searchDao.searchQuote("quote")
    val matchedQuote: SearchResponse                      = Await.result(getMatchedQuote, Duration.Inf).result

    matchedQuote.to[QuoteWithAuthor].toList.map(_.quoteDetails.csvId).sorted shouldBe Seq(
      "csv101",
      "csv102",
      "csv103"
    )
  }

  it should "perform search query using match query with fuzziness if match prefix return nil" in {
    val getMatchedQuote: Future[Response[SearchResponse]] = searchDao.searchQuote("QuOt")
    val matchedQuote: SearchResponse                      = Await.result(getMatchedQuote, Duration.Inf).result

    matchedQuote.to[QuoteWithAuthor].toList.map(_.quoteDetails.csvId).sorted shouldBe Seq(
      "csv101",
      "csv102",
      "csv103"
    )
  }

  it should "return empty if not found" in {
    val getMatchedQuote: Future[Response[SearchResponse]] = searchDao.searchQuote("wrong text")
    val matchedQuote: SearchResponse                      = Await.result(getMatchedQuote, Duration.Inf).result

    matchedQuote.to[QuotesQuery].toList shouldBe Seq.empty
  }

  it should "return result with correct limit" in {
    val getMatchedQuote: Future[Response[SearchResponse]] = searchDao.searchQuote("quote", limit = 1)
    val matchedQuote: SearchResponse                      = Await.result(getMatchedQuote, Duration.Inf).result
    matchedQuote.hits.size shouldBe 1
  }

  it should "return result with correct offset" in {
    val getMatchedQuote: Future[Response[SearchResponse]] = searchDao.searchQuote("Test", offset = 1)
    val matchedQuote: SearchResponse                      = Await.result(getMatchedQuote, Duration.Inf).result
    matchedQuote.hits.size shouldBe 1
  }

  it should "return auto completion for all the matched author names" in {
    val result: Seq[String] = checkAutoCompletion("author", isPrefixMatch = true)
    result shouldBe Seq("author1 Test", "author2 Test", "random author")
  }

  it should "return auto completion for the exact match author name" in {
    val result: Seq[String] = checkAutoCompletion("random", isPrefixMatch = true)
    result shouldBe Seq("random author")
  }

  it should "return auto completion for the matched author with typo in searched text" in {
    val result: Seq[String] = checkAutoCompletion("auphor1")
    result shouldBe Seq("author1 Test", "author2 Test")
  }

  it should "return empty if it is not found in the ES" in {
    val result: Seq[String] = checkAutoCompletion("invalid")
    result shouldBe Seq.empty
  }

  it should "delete the index in ES" in {
    val isDeleted: Boolean =
      Await
        .result(searchDao.deleteQuotesIndex(searchDao.indexName), Duration.Inf)
        .result
        .acknowledged
    isDeleted shouldBe true
  }

  private def checkAutoCompletion(searchedText: String, isPrefixMatch: Boolean = false): Seq[String] = {
    val getAuthorCompletion: Future[Response[SearchResponse]] = searchDao.completeAuthorNames(searchedText)
    val autoCompletionAuthors: SearchResponse                 = Await.result(getAuthorCompletion, Duration.Inf).result

    if (isPrefixMatch) autoCompletionAuthors.to[AuthorSearchResponse].toList.map(_.quoteDetails.author)
    else
      autoCompletionAuthors
        .suggestions(CompletionCustomSuggestion.suggestionName)
        .flatMap(_.toCompletion.options)
        .map(_.text)
  }
}
