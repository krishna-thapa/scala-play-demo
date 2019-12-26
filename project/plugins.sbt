// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.3")

// play swagger plugin
addSbtPlugin("com.iheart" % "sbt-play-swagger" % "0.9.0-PLAY2.7")

// build an independent jar file which contains the application as well as its dependencies
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")