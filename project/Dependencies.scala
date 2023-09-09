import sbt._

object Dependencies {

  val distCacheApi: Seq[ModuleID] = Seq(
    "org.json4s" %% "json4s-native" % "3.6.0-M2"
  )

  val distCacheLib: Seq[ModuleID] = Seq(
    "org.json4s" %% "json4s-native" % "4.0.6",
    "commons-dbcp" % "commons-dbcp" % "1.4",
    "org.apache.httpcomponents" % "httpclient" % "4.5.13",
    "org.slf4j" % "slf4j-api" % "2.0.3",
    "ch.qos.logback" % "logback-core" % "1.4.4",
    "ch.qos.logback" % "logback-classic" % "1.4.4",
    "org.junit.jupiter" % "junit-jupiter-engine" % "5.9.0" % "test",
    "org.mockito" % "mockito-core" % "4.8.1" % "test"
  )

  val distCacheApp: Seq[ModuleID] = Seq(
    "org.json4s" %% "json4s-native" % "3.6.0-M2"
  )

}
