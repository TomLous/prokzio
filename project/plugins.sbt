// Format Scala code https://github.com/scalameta/scalafmt / doesn't need to align with scalafmt version
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// Native Image on local machine https://github.com/scalameta/sbt-native-image
addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.0")

// Package code natively. Used for Graal \w Docker https://github.com/sbt/sbt-native-packager
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.0")
