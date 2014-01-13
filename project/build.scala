import sbt._
import sbt.Keys._
import sbt.Def._
import java.io.File

object MyApp extends sbt.Build {

  // heroku
  val stage = TaskKey[Unit]("stage", "Heroku installation hook")
  val script = TaskKey[File]("script", "Generates script-file")
  def stageTask(script: File) = { /* noop */ }
  private def rootDir(state: State) =
    file(Project.extract(state).structure.root.toURL.getFile)
  private def relativeToRoot(state: State, f: File) =
    IO.relativize(rootDir(state), f)
  private def scriptTask: Initialize[Task[File]] =
    (mainClass in Runtime,
     streams,
     fullClasspath in Runtime,
     state,
     target) map {
      (main, out, cp, state, target) => main match {
        case Some(mainCls) =>
          val scriptBody = Script(mainCls, cp.files map { f =>
            relativeToRoot(state, f) match {
              case Some(rel) => rel
              case _ => f.getAbsolutePath
            }
          }, Seq("-Xmx256m","-Xss2048k"))
          val sf = new File(target, "hero")
          out.log.info("Writing hero file, %s" format sf)
          IO.write(sf, scriptBody)
          sf.setExecutable(true)
          sf
        case _ => sys.error("Main class required")
      }
    }

  lazy val root =
    Project("root", file("."),
      settings = sbt.Defaults.defaultSettings ++ /*heroic.Plugin.heroicSettings*/ Seq(
        script <<= scriptTask,
        stage in Compile <<= script map stageTask
      )
    )// dependsOn(dispatchMeetup)

  // git-dependencies don't work on Heroku so we use submodules
  // don't forget:
  // git submodule init
  // git submodule update

/*  lazy val dispatchLiftJson = file("lib/dispatch-lift-json")

  lazy val dispatchMeetup = Project(
    "dispatch-meetup",
    file("lib/dispatch-meetup")
  ) dependsOn (dispatchLiftJson)*/
}
