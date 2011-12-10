import sbt._
object MyApp extends Build
{
  lazy val root =
    Project("", file(".")) dependsOn(dispatchMeetup)
  lazy val dispatchMeetup =
    uri("git://github.com/n8han/dispatch-meetup#0.1.2")
}
