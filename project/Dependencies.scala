import sbt._

object Dependencies {

  object Versions {
    val postgresql     = "42.2.12"
    val playSlick      = "4.0.2"
    val scalaTest      = "3.2.0"
    val swagger        = "2.2.0"
    val testcontainers = "0.36.0"
    val playtest       = "5.0.0"
  }

  object Libraries {
    def playSlickLib(artifact: String): ModuleID =
      "com.typesafe.play" %% artifact % Versions.playSlick
    def postgresLib(artifact: String): ModuleID  = "org.postgresql" % artifact  % Versions.postgresql
    def scalaTestLib(artifact: String): ModuleID = "org.scalatest"  %% artifact % Versions.scalaTest
    def swaggerLib(artifact: String): ModuleID   = "org.webjars"    % artifact  % Versions.swagger
    def testcontainersLib(artifact: String): ModuleID =
      "com.dimafeng" %% artifact % Versions.testcontainers
    def playScalaTestlib(artifact: String): ModuleID =
      "org.scalatestplus.play" %% artifact % Versions.playtest

    val playSlick               = playSlickLib("play-slick")
    val playSlickEvolutions     = playSlickLib("play-slick-evolutions")
    val postgres                = postgresLib("postgresql")
    val scalaTest               = scalaTestLib("scalatest") % "test"
    val swaggerUi               = swaggerLib("swagger-ui")
    val testcontainersScalaTest = testcontainersLib("testcontainers-scala-scalatest") % "test"
    val testcontainersPostgres  = testcontainersLib("testcontainers-scala-postgresql") % "test"
    val playScalaTest           = playScalaTestlib("scalatestplus-play") % "test"
  }
}
