seq(lsSettings :_*)

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-filter" % "0.5.3",
  "net.databinder" %% "unfiltered-jetty" % "0.5.3",
  "net.databinder" %% "unfiltered-json" % "0.5.3",
  "net.databinder" %% "dispatch-oauth" % "0.8.6",
  "net.debasishg" %% "redisclient" % "2.4.2"
)

scalacOptions += "-deprecation"
