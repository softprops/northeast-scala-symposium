object MyApp extends sbt.Build {
  import sbt._
  import sbt.Keys._
  lazy val root =
    Project("", file("."),
      settings = sbt.Defaults.defaultSettings /*++ Heroku.herokuSettings */ ++ heroic.Plugin.heroicSettings
    ) dependsOn(dispatchMeetup)

  // git-dependencies don't work on Heroku so we use submodules
  // don't forget:
  // git submodule init
  // git submodule update

  lazy val dispatchLiftJson = file("lib/dispatch-lift-json")

  lazy val dispatchMeetup = Project(
    "dispatch-meetup",
    file("lib/dispatch-meetup")
  ) dependsOn (dispatchLiftJson)
}
