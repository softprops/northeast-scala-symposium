seq(lsSettings :_*)

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-filter" % "0.7.1",
  "net.databinder" %% "unfiltered-jetty" % "0.7.1",
  "net.databinder" %% "unfiltered-json4s" % "0.7.1",
  // http client
  "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.10.0",
  // persistance
  "net.debasishg" %% "redisclient" % "2.13",
  // loging
  "org.slf4j" % "slf4j-jdk14" % "1.6.2",
  // date math
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2",
  // local cache
  "com.google.guava" % "guava" % "14.0",
  "com.google.code.findbugs" % "jsr305" % "3.0.0" // http://stackoverflow.com/questions/19030954/cant-find-nullable-inside-javax-annotation
)

scalacOptions ++= Seq("-deprecation", "-unchecked")

seq(Revolver.settings: _*)
