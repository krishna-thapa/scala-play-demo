package daos

import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import play.api.db.{ Database, Databases }
import play.api.db.evolutions.Evolutions

trait ConfigDb extends PlaySpec with BeforeAndAfterAll {

  /**
    * Here we use Databases.withDatabase to run evolutions without leaking connections.
    */
  private def withTestDatabase[T](block: Database => T) = {
    Databases.withInMemory(
      name = "mydatabase",
      urlOptions = Map(
        "MODE" -> "PostgreSQL"
      ),
      config = Map(
        "logStatements" -> true
      )
    )(block)
  }

  override def beforeAll(): Unit = {
    withTestDatabase { database =>
      Evolutions.applyEvolutions(database)
    }
  }

  override def afterAll(): Unit = {
    withTestDatabase { database =>
      Evolutions.cleanupEvolutions(database)
      database.shutdown()
    }
  }

}
