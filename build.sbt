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
  "org.postgresql"         % "postgresql"             % "42.2.12",
  "com.typesafe.play"      %% "play-slick"            % "4.0.2",
  "com.typesafe.play"      %% "play-slick-evolutions" % "4.0.2",
  "com.byteslounge"        %% "slick-repo"            % "1.5.3",
  "org.slf4j"              % "slf4j-nop"              % "1.6.4",
  "org.webjars"            % "swagger-ui"             % "2.2.0"
)

//add domain package names for play-swagger to auto generate swagger definitions for domain classes mentioned in your routes
swaggerDomainNameSpaces := Seq("models")

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings",
  // Enable routes file splitting
  "-language:reflectiveCalls"
)
