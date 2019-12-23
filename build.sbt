name := """inspirational-quotes"""
organization := "com.krishna"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  guice, evolutions,
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.typesafe.play" %% "play-slick" % "4.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.2",
  "com.byteslounge" %% "slick-repo" % "1.5.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.h2database" % "h2" % "1.4.199",
  "org.webjars" % "swagger-ui" % "2.2.0"  //play-swagger ui integration
)

swaggerDomainNameSpaces := Seq("models")

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings"
)
