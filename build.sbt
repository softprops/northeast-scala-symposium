seq(lsSettings :_*)

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-filter" % "0.7.1",
  "net.databinder" %% "unfiltered-jetty" % "0.7.1",
  "net.databinder" %% "unfiltered-json4s" % "0.7.1",
  "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.10.0",
  "net.debasishg" %% "redisclient" % "2.13",
  "org.slf4j" % "slf4j-jdk14" % "1.6.2",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2"
)

scalacOptions ++= Seq("-deprecation", "-unchecked")

seq(Revolver.settings: _*)
