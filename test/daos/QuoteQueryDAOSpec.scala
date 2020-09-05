package daos

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import javax.inject.Inject
import models.QuotesQuery
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.{ OneAppPerSuite, PlaySpec }
import play.api.{ Application, Mode, Play }
import play.api.test.Helpers._
import play.api.db.evolutions._
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.{ JdbcBackend, JdbcProfile }
import slick.profile.RelationalProfile
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcBackend.Database

class QuoteQueryDAOSpec extends AnyFlatSpec with TestContainerForAll with Matchers {

  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def()

  lazy val appBuilder: GuiceApplicationBuilder    = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector                     = appBuilder.injector()
  lazy val dbConfProvider: DatabaseConfigProvider = injector.instanceOf[DatabaseConfigProvider]

  private val quoteQueryDAO = new QuoteQueryDAO(dbConfProvider)

  behavior of "QuoteQueryDAO"

  it should "return a random quote" in {
    val result = quoteQueryDAO.listRandomQuote(1)

  }
}
