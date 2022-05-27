import Dependencies.Libraries._
import scala.language.postfixOps

name := """inspirational-quote-api"""
description := "Back-end project for Inspirational quotes"
version := "1.0-SNAPSHOT"

ThisBuild / organization := "com.krishna"
ThisBuild / scalaVersion := "2.13.8"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .settings(
    libraryDependencies ++=
      commonDependencies ++ Seq(swaggerUi),
    Test / fork := true
  )
  .aggregate(quotes, customQuotes, auth, search, common)
  .dependsOn(quotes, customQuotes, auth, search, common)

lazy val quotes = project
  .in(file("modules/quotes"))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .dependsOn(common, auth)
  .settings(
    libraryDependencies ++=
      commonDependencies ++
        akka ++
        slickDatabaseDependencies ++
        testContainerDependencies ++
        Seq(
          cacheApi,
          playRedis,
          quartzScheduler
        )
  )

lazy val customQuotes = project
  .in(file("modules/customQuotes"))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .dependsOn(quotes % "compile->compile;test->test", common, auth)
  .settings(
    libraryDependencies ++=
      commonDependencies ++
        slickDatabaseDependencies ++
        testContainerDependencies
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

// Configuration for sbt-dependency-check
// https://github.com/albuch/sbt-dependency-check#configuration
dependencyCheckFormats := Seq("JSON", "HTML")
dependencyCheckSkipOptionalScope := true
dependencyCheckSkipProvidedScope := true
dependencyCheckSkipRuntimeScope := true

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings",
  // Enable routes file splitting
  "-language:reflectiveCalls"
)
