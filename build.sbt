seq(lsSettings :_*)

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-filter" % "0.6.4",
  "net.databinder" %% "unfiltered-jetty" % "0.6.4",
  "net.databinder" %% "unfiltered-json" % "0.6.4",
  "net.databinder" %% "dispatch-oauth" % "0.8.8",
  "net.debasishg" %% "redisclient" % "2.4.2",
  "org.slf4j" % "slf4j-jdk14" % "1.6.2"
)

scalacOptions ++= Seq("-deprecation", "-unchecked")
