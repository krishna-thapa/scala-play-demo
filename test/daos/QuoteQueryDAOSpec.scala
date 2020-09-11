package daos

import play.api.Mode
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder

class QuoteQueryDAOSpec extends ConfigDb {

  lazy val appBuilder: GuiceApplicationBuilder    = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector                     = appBuilder.injector()
  lazy val dbConfProvider: DatabaseConfigProvider = injector.instanceOf[DatabaseConfigProvider]

  private val quoteQueryDAO = new QuoteQueryDAO(dbConfProvider)

  "QuoteQueryDAO" should {
    "return a random quote" in {
      val result = quoteQueryDAO.listRandomQuote(1)
      println(result)
    }
  }

}
