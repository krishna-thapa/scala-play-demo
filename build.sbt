import Dependencies.Libraries._
import scala.language.postfixOps

name := """inspirational-quote-api"""
description := "Back-end project for Inspirational quotes"
version := "1.0-SNAPSHOT"

organization in ThisBuild := "com.krishna"
scalaVersion in ThisBuild := "2.13.6"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .settings(
    libraryDependencies ++=
      commonDependencies ++
        testContainerDependencies ++
        slickDatabaseDependencies ++ Seq(
        swaggerUi
      ),
    Test / fork := true
  )
  .aggregate(quotes, auth, search, common)
  // https://stackoverflow.com/questions/8193904/sbt-test-dependencies-in-multiprojects-make-the-test-code-available-to-dependen
  .dependsOn(quotes % "compile->compile;test->test", auth, search, common)

lazy val quotes = project
  .in(file("modules/quotes"))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .dependsOn(common, auth)
  .settings(
    libraryDependencies ++=
      commonDependencies ++
        slickDatabaseDependencies ++
        testContainerDependencies ++
        Seq(
          cacheApi,
          playRedis,
          quartzScheduler
        )
  )

lazy val auth = project
  .in(file("modules/auth"))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .dependsOn(common)
  .settings(
    libraryDependencies ++=
      commonDependencies ++
        slickDatabaseDependencies ++
        mongoDependencies ++
        Seq(
          playJwt,
          scalaBcrypt
        )
  )

lazy val search = project
  .in(file("modules/search"))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .dependsOn(common, auth, quotes)
  .settings(
    libraryDependencies ++=
      commonDependencies ++
        elastic4sDependencies ++
        akka ++
        dockerTestKitWithMock
  )

lazy val common = project
  .in(file("modules/common"))
  .settings(
    name := "common",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++=
      commonDependencies ++
        slickDatabaseDependencies ++
        Seq(ws)
  )

// https://www.scala-sbt.org/1.x/docs/Tasks.html

// Creating a custom sbt task for the docker start-up
import scala.sys.process._
lazy val sbtDockerStart = taskKey[Unit]("Start the docker containers")
sbtDockerStart := {
  "./scripts/docker_start.sh" !
}

// Creating a custom sbt task for running project using sbt run
lazy val sbtLocalStart = taskKey[Unit]("Start the project using sbt shell")
sbtLocalStart := {
  "./scripts/sbt_start.sh" !
}

// Creating a custom sbt task for the csv migration to the docker container
lazy val sbtCsvMigrate = taskKey[Unit]("Migrate the CSV to the Postgres table")
sbtCsvMigrate := {
  "./scripts/csv_migration.sh" !
}

// Creating a custom sbt task for running project's test coverage and opening coverage report in google chrome
lazy val sbtScoverageDoc = taskKey[Unit]("Get the project's scoverage report in html")
sbtScoverageDoc := {
  "./scripts/scoverage_html.sh" !
}

/*
 add domain package names for play-swagger to auto generate swagger
 definitions for domain classes mentioned in your routes
 */
swaggerDomainNameSpaces := Seq(
  "models",
  "model",
  "forms",
  "form",
  "searchForm",
  "com.krishna.response",
  "com.krishna.model"
)

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings",
  // Enable routes file splitting
  "-language:reflectiveCalls"
)
