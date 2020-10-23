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
      playJwt,
      scalaBcrypt,
      cacheApi,
      playRedis,
      postgres,
      playSlickEvolutions,
      playSlick,
      //scalaTest,
      swaggerUi,
      //playScalaTest,
      elastic4s,
      elastic4sJson
    ),
    Test / fork := true
  )
/*
 add domain package names for play-swagger to auto generate swagger
 definitions for domain classes mentioned in your routes
 */
swaggerDomainNameSpaces := Seq("models", "forms", "response", "auth", "search")

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings",
  // Enable routes file splitting
  "-language:reflectiveCalls"
)
