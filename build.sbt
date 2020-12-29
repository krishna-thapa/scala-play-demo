import Dependencies.Libraries._

name := """inspirational-quote-api"""
description := "Back-end project for Inspirational quotes"
version := "1.0-SNAPSHOT"

organization in ThisBuild := "com.krishna"
scalaVersion in ThisBuild := "2.13.1"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .settings(
    libraryDependencies ++=
      commonDependencies ++
        slickDatabaseDependencies ++ Seq(
        //scalaTest,
        swaggerUi
        //playScalaTest
      ),
    Test / fork := true
  )
  .aggregate(quotes, auth, search, common)
  .dependsOn(quotes, auth, search, common)

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
          quartzScheduler,
          scalaTest,
          playScalaTest
          //"org.mockito" %% "mockito-scala-scalatest" % "1.13.0" % "test"
          //"com.typesafe.akka" %% "akka-testkit" % "2.6.10" % Test
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
        elastic4sDependencies
  )

lazy val common = project
  .in(file("modules/common"))
  .settings(
    name := "common",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++=
      commonDependencies ++
        elastic4sDependencies ++
        slickDatabaseDependencies
  )

lazy val commonDependencies = Seq(
  guice
)

lazy val slickDatabaseDependencies = Seq(
  //jdbc,
  postgres,
  playSlickEvolutions,
  playSlick
)

lazy val testContainerDependencies = Seq(
  testContainers,
  postgresContainer
)

lazy val elastic4sDependencies = Seq(
  elastic4s,
  elastic4sJson
)

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
