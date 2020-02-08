import com.sun.xml.internal.bind.v2.TODO

name := """inspirational-quote-api"""
organization := "com.krishna"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  guice,
  evolutions,
  "org.scalatestplus.play" %% "scalatestplus-play"    % "4.0.3" % Test,
  "org.postgresql"         % "postgresql"             % "9.4-1201-jdbc41",
  "com.typesafe.play"      %% "play-slick"            % "4.0.2",
  "com.typesafe.play"      %% "play-slick-evolutions" % "4.0.2",
  "com.byteslounge"        %% "slick-repo"            % "1.5.3",
  "org.slf4j"              % "slf4j-nop"              % "1.6.4",
  "com.h2database"         % "h2"                     % "1.4.199",
  "org.webjars"            % "swagger-ui"             % "2.2.0" //play-swagger ui integration
)

//add domain package names for play-swagger to auto generate swagger definitions for domain classes mentioned in your routes
swaggerDomainNameSpaces := Seq("models")

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings",
  // Enable routes file splitting
  "-language:reflectiveCalls"
)

//TODO: this below codes
// No need to run tests while building jar
test in assembly := {}
// Simple and constant jar name
assemblyJarName in assembly := s"inspirational-quote-api.jar"
// Merge strategy for assembling conflicts
assemblyMergeStrategy in assembly := {
  case PathList("reference.conf")          => MergeStrategy.concat
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _                                   => MergeStrategy.first
}
