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

lazy val dockerTestKitWithMock = Seq(
  mockito,
  testContainers,
  elasticSearchContainer
)

val AkkaVersion = "2.6.13"
lazy val akka = Seq(
  "com.typesafe.akka" %% "akka-stream"                % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed"           % AkkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
  "com.typesafe.akka" %% "akka-slf4j"                 % AkkaVersion
)

lazy val commonDependencies = Seq(
  guice,
  scalaTest,
  logbackEncoder intransitive ()
)

lazy val slickDatabaseDependencies = Seq(
  //jdbc,
  postgres,
  playSlickEvolutions,
  playSlick
)

lazy val testContainerDependencies = Seq(
  testContainers,
  postgresContainer,
  playScalaTest // Need this?
)

lazy val elastic4sDependencies = Seq(
  elastic4s,
  elastic4sJson,
  elastic4sStreams
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
