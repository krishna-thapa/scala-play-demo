package helper

import com.dimafeng.testcontainers.PostgreSQLContainer
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

trait PlayPostgreSQLTest {
  def getDb(container: PostgreSQLContainer): JdbcBackend.DatabaseDef = {
    Database.forURL(
      url = container.jdbcUrl,
      user = container.username,
      password = container.password,
      driver = "slick.jdbc.PostgresProfile$"
    )
  }
}
