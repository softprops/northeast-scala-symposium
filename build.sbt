seq(lsSettings :_*)

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-filter" % "0.7.1",
  "net.databinder" %% "unfiltered-jetty" % "0.7.1",
  "net.databinder" %% "unfiltered-json4s" % "0.7.1",
  "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.11.0",
  "net.debasishg" %% "redisclient" % "2.10",
  "org.slf4j" % "slf4j-jdk14" % "1.6.2")

scalacOptions ++= Seq("-deprecation", "-unchecked")
