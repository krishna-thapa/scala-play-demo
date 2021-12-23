package akkaService

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.alpakka.csv.scaladsl.{ CsvParsing, CsvToMap }
import akka.stream.scaladsl.{ FileIO, Flow, Source }
import akka.util.ByteString
import com.krishna.akkaStream.StreamsInit
import daos.QuoteQueryDAO
import httpService.{ ExtendedWikiMediaApi, FinalQuotes, FinalQuotesWithAuthor }
import play.api.Configuration

import java.io.File
import java.nio.file.Paths
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class ExtendedAkkaService @Inject()(
    quotesDAO: QuoteQueryDAO,
    wikiMediaApi: ExtendedWikiMediaApi,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends StreamsInit {

  override def batchSize: Int = config.get[Int]("csvLoad.batchSize")

  override val dbConfig = quotesDAO.dbConfig

  implicit lazy val system: ActorSystem = ActorSystem()

  def runCSVMigrationStream: Future[String] = {

    log.info("Testing the CSV data migration to the Postgres Database using Akka stream with Alpakka")

    val quotesCsvFile: File = new File("data/New-Quotes.csv")

    if (!quotesCsvFile.isFile) return Future.successful("Quotes.csv file not found under data directory")

    val csvFileName: String = quotesCsvFile.getName
    val csvFilePath: String = quotesCsvFile.getPath
    log.info(s"File with the name: $csvFileName and with path: $csvFilePath")

    val csvSource: Source[ByteString, Future[IOResult]] = FileIO.fromPath(Paths.get(csvFilePath))

    def convertToQuoteObject(): Flow[Map[String, String], FinalQuotes, NotUsed] = {
      Flow[Map[String, String]].mapAsync(parallelism = 1) { quote =>
        Future.successful(
          FinalQuotes(
            quote = quote("quote"),
            author = quote.get("author").filterNot(_.trim.isEmpty),
            genre = quote.get("category").toList.flatMap(_.split(",")).map(_.trim)
          )
        )
      }
    }

    // val startTime: Long = System.nanoTime

    csvSource
      .via(CsvParsing.lineScanner())
      .via(CsvToMap.toMapAsStrings())
      .via(convertToQuoteObject())
      .via(addFlowPerRecord[FinalQuotes, FinalQuotesWithAuthor](wikiMediaApi.getWikiResponse))
      .alsoTo(logElementsPerBlock)
      .runForeach(quoteWithAuthor => log.info(s""))
      .map { _ =>
        log.info(s"Successfully processed the file $csvFileName")
        "Success"
      }
      .recover {
        case e: Exception =>
          log.error(s"Error in processing the file $csvFileName")
          s"Failed with error: ${e.getMessage}"
      }

    // Await.result(result, Duration.Inf)
    // val runTimeDuration = (System.nanoTime - startTime) / 1e9d
    // log.info(s"Total run time duration for the Akka Stream $runTimeDuration")
  }
}
