// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.4")

// play swagger plugin - https://github.com/iheartradio/play-swagger
addSbtPlugin("com.iheart" % "sbt-play-swagger" % "0.10.2-PLAY2.8")

// scalafmt plugin for scala formatter - https://github.com/scalameta/sbt-scalafmt
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// https://github.com/mefellows/sbt-dotenv
addSbtPlugin("au.com.onegeek" % "sbt-dotenv" % "2.1.204")

// https://github.com/dwijnand/sbt-reloadquick
addSbtPlugin("com.dwijnand" % "sbt-reloadquick" % "1.0.0")
