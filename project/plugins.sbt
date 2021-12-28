// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.11")

// play swagger plugin - https://github.com/iheartradio/play-swagger
addSbtPlugin("com.iheart" % "sbt-play-swagger" % "0.10.6-PLAY2.8")

// scalafmt plugin for scala formatter - https://github.com/scalameta/sbt-scalafmt
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.3")

// https://github.com/mefellows/sbt-dotenv
addSbtPlugin("au.com.onegeek" % "sbt-dotenv" % "2.1.204")

// https://www.baeldung.com/scala/sbt-dependency-tree
// sbt dependencyBrowseGraph
// sbt dependencyBrowseTree
addDependencyTreePlugin

// https://github.com/rtimush/sbt-updates
// sbt dependencyUpdates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.0")

// https://github.com/DavidGregory084/sbt-tpolecat
// addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.20")

// https://github.com/scoverage/sbt-scoverage
// https://github.com/scoverage/scalac-scoverage-plugin
// sbt sbtScoverageDoc
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.1")

// https://github.com/albuch/sbt-dependency-check
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "3.3.0")
