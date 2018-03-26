name := "manalyser"

version := "1.0"

scalaVersion := "2.12.4"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= {
  val akkaVersion = "2.5.3"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",

    "org.mongodb.scala" %% "mongo-scala-driver" % "2.2.1",

    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.specs2" %% "specs2-core" % "3.9.0" % Test,

    "org.scalaz" %% "scalaz-core" % "7.3.0-M10",
    "com.googlecode.charts4j" % "charts4j" % "1.3",

    "org.apache.commons" % "commons-math3" % "3.6.1",
    "info.mukel" %% "telegrambot4s" % "2.2.1-SNAPSHOT",
    "jfree" % "jfreechart" % "1.0.13",

    "org.telegram" % "telegramapi" % "66.2",

    "com.caffeineowl" % "bezier-utils" % "1.0.0-RELEASE"
  )
}
