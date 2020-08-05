// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.3")

// play swagger plugin
addSbtPlugin("com.iheart" % "sbt-play-swagger" % "0.9.0-PLAY2.7")

// scalafmt plugin for scala formatter
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.2.1")
