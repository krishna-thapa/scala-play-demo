import Dependencies.Libraries._

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .settings(
    name := """inspirational-quote-api""",
    organization := "com.krishna",
    description := "Back-end project for Inspirational quotes",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      jdbc,
      guice,
      cacheApi,
      playRedis,
      postgres,
      playSlickEvolutions,
      playSlick,
      //scalaTest,
      swaggerUi,
      playScalaTest,
      h2Database
    ),
    Test / fork := true
  )
/*
 add domain package names for play-swagger to auto generate swagger
 definitions for domain classes mentioned in your routes
 */
swaggerDomainNameSpaces := Seq("models", "forms", "response")

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings",
  // Enable routes file splitting
  "-language:reflectiveCalls"
)
