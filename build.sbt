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
    libraryDependencies ++= commonDependencies ++ Seq(
      jdbc,
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
  .aggregate(auth, common)
  .dependsOn(auth, common)

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
  postgres,
  playSlickEvolutions,
  playSlick
)

lazy val elastic4sDependencies = Seq(
  elastic4s,
  elastic4sJson
)

/*
 add domain package names for play-swagger to auto generate swagger
 definitions for domain classes mentioned in your routes
 */
swaggerDomainNameSpaces := Seq("models", "model", "forms", "form", "search")

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings",
  // Enable routes file splitting
  "-language:reflectiveCalls"
)
