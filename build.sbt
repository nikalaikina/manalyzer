name := "manalyser"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaVersion = "2.4.6"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",

    "org.mongodb.scala" %% "mongo-scala-driver" % "1.0.1",

    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.specs2" %% "specs2" % "2.3.13" % "test",

    "org.scalaz" %% "scalaz-core" % "7.3.0-M10",
    "com.googlecode.charts4j" % "charts4j" % "1.3",

    "org.apache.commons" % "commons-math3" % "3.6.1",

    "org.telegram" % "telegramapi" % "57.2"
  )
}
