import Main.system.dispatcher
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.alpakka.csv.scaladsl.{ CsvParsing, CsvToMap }
import akka.stream.scaladsl.{ FileIO, Flow, Source }
import akka.util.ByteString
import com.krishna.util.Logging

import java.io.File
import java.nio.file.Paths
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

object Main extends App with Logging {

  log.info("For the testing purpose only!!")
  log.info("Testing the CSV data migration to the Postgres Database using Akka stream with Alpakka")

  implicit val system: ActorSystem = ActorSystem()

  val quotesCsvFile: File = new File("data/New-Quotes.csv")
  val csvFileName: String = quotesCsvFile.getName
  val csvFilePath: String = quotesCsvFile.getPath
  log.info(s"File with the name: $csvFileName and with path: $csvFilePath")

  val csvSource: Source[ByteString, Future[IOResult]] = FileIO.fromPath(Paths.get(csvFilePath))

  val startTime: Long = System.nanoTime

  val result: Future[Boolean] = csvSource
    .via(CsvParsing.lineScanner())
    .via(CsvToMap.toMapAsStrings())
    .via(convertToQuoteObject())
    .runForeach(quote => log.info(s"$quote"))
    .map { _ =>
      log.info(s"Successfully processed the file $csvFileName")
      true
    }
    .recover {
      case _: Exception =>
        log.error(s"Error in processing the file $csvFileName")
        false
    }

  Await.result(result, Duration.Inf)
  val runTimeDuration = (System.nanoTime - startTime) / 1e9d
  log.info(s"Total run time duration for the Akka Stream $runTimeDuration")

  case class FinalQuotes(quote: String, author: Option[String], genre: Seq[String])
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
}
