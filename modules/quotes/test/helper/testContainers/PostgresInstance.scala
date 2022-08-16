package helper.testContainers

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import helper.LoadQueries
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatestplus.play.GuiceOneAppPerSuiteAsync
import org.testcontainers.utility.DockerImageName
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

trait PostgresInstance
    extends AsyncFlatSpec
    with GuiceOneAppPerSuiteAsync
    with TestContainerForAll
    with LoadQueries {

  // Define a Postgres Container for test docker
  override val containerDef: PostgreSQLContainer.Def =
    PostgreSQLContainer.Def(DockerImageName.parse("postgres:latest"))

  // Start the connection
  val containerPostgres: PostgreSQLContainer = startContainers()

  // Initialize the Database connection using PostgresSQLContainer
  override val dbConfig: JdbcBackend.DatabaseDef = {
    Database.forURL(
      url = containerPostgres.jdbcUrl,
      user = containerPostgres.username,
      password = containerPostgres.password,
      driver = "org.postgresql.Driver"
    )
  }

  // Create and inject the instance of DatabaseConfigProvider
  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "slick.dbs.default.profile" -> "slick.jdbc.PostgresProfile$",
      "slick.dbs.default.db.driver" -> "org.postgresql.Driver",
      "slick.dbs.default.db.url" -> containerPostgres.jdbcUrl,
      "slick.dbs.default.db.user" -> containerPostgres.username,
      "slick.dbs.default.db.password" -> containerPostgres.password,
      "play.evolutions.db.default.enabled" -> "false" // Important to disable evolution while running test
    )
    .build()

}
