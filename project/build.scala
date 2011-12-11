object MyApp extends sbt.Build {
  import sbt._
  import sbt.Keys._
  lazy val root =
    Project("", file("."),
      settings = sbt.Defaults.defaultSettings ++ Seq(
        scalacOptions += "-deprecation",
        libraryDependencies ++= Seq(
          "net.debasishg" %% "redisclient" % "2.4.2"
        )
      ) ++ Heroku.herokuSettings /*++ heroic.Plugin.heroicSettings*/
    ) dependsOn(dispatchMeetup)

  lazy val dispatchMeetup =
    uri("git://github.com/n8han/dispatch-meetup#0.1.2")
}
