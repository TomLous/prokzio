// Format Scala code https://github.com/scalameta/scalafmt / doesn't need to align with scalafmt version
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// Native Image on local machine https://github.com/scalameta/sbt-native-image
addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.0")

// Package code natively. Used for Graal \w Docker https://github.com/sbt/sbt-native-packager
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.0")

// sbt plugin for Scala Code Coverage: https://github.com/scoverage/sbt-scoverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")

// Automated release process https://github.com/sbt/sbt-release
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.0.15")

// Generare code based on proto https://github.com/scalapb/zio-grpc
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.2")
libraryDependencies += "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.4.4"
