import sbt._

object Dependencies {

  object Versions {
    val playSlick       = "5.0.0"
    val swagger         = "3.43.0"
    val playjwt         = "5.0.0"
    val bcrypt          = "4.3.0"
    val quartzScheduler = "1.8.5-akka-2.6.x"
    val akkaVersion     = "2.6.13"

    // Database
    val monogoDb   = "1.0.4-play28"
    val elastic4s  = "7.9.1"
    val playRedis  = "2.6.1"
    val postgresql = "42.2.12"

    // tests
    val scalaTest      = "3.2.0"
    val playtest       = "5.0.0"
    val testContainers = "0.38.8"
    val mockitoVer     = "1.13.0"

    // logs
    val logbackEncoder = "6.6"
  }

  object Libraries {
    val playSlick           = "com.typesafe.play" %% "play-slick"            % Versions.playSlick
    val playSlickEvolutions = "com.typesafe.play" %% "play-slick-evolutions" % Versions.playSlick
    val swaggerUi           = "org.webjars"       % "swagger-ui"             % Versions.swagger
    val playJwt             = "com.pauldijou"     %% "jwt-play"              % Versions.playjwt
    val scalaBcrypt         = "com.github.t3hnar" %% "scala-bcrypt"          % Versions.bcrypt
    val quartzScheduler     = "com.enragedginger" %% "akka-quartz-scheduler" % Versions.quartzScheduler

    // Akka
    val akkaStream  = "com.typesafe.akka" %% "akka-stream"                % Versions.akkaVersion
    val akkaActor   = "com.typesafe.akka" %% "akka-actor-typed"           % Versions.akkaVersion
    val akkaJackson = "com.typesafe.akka" %% "akka-serialization-jackson" % Versions.akkaVersion
    val akkaSlf4j   = "com.typesafe.akka" %% "akka-slf4j"                 % Versions.akkaVersion

    // Database
    val reactivemongo     = "org.reactivemongo"      %% "play2-reactivemongo"            % Versions.monogoDb
    val reactivemongoJson = "org.reactivemongo"      %% "reactivemongo-play-json-compat" % Versions.monogoDb
    val elastic4s         = "com.sksamuel.elastic4s" %% "elastic4s-client-esjava"        % Versions.elastic4s
    val elastic4sJson     = "com.sksamuel.elastic4s" %% "elastic4s-json-play"            % Versions.elastic4s
    val elastic4sStreams  = "com.sksamuel.elastic4s" %% "elastic4s-http-streams"         % Versions.elastic4s
    val playRedis         = "com.github.karelcemus"  %% "play-redis"                     % Versions.playRedis
    val postgres          = "org.postgresql"         % "postgresql"                      % Versions.postgresql

    // logs
    val logbackEncoder =
      "net.logstash.logback" % "logstash-logback-encoder" % Versions.logbackEncoder excludeAll ExclusionRule(
        organization = "com.fasterxml.jackson.core"
      )

    // tests
    val scalaTest              = "org.scalatest"          %% "scalatest"                          % Versions.scalaTest      % "test"
    val playScalaTest          = "org.scalatestplus.play" %% "scalatestplus-play"                 % Versions.playtest       % "test"
    val testContainers         = "com.dimafeng"           %% "testcontainers-scala-scalatest"     % Versions.testContainers % "test"
    val postgresContainer      = "com.dimafeng"           %% "testcontainers-scala-postgresql"    % Versions.testContainers % "test"
    val elasticSearchContainer = "com.dimafeng"           %% "testcontainers-scala-elasticsearch" % Versions.testContainers % "test"

    // mock
    val mockito = "org.mockito" %% "mockito-scala-scalatest" % Versions.mockitoVer % "test"
  }
}
