import sbt._
object PluginDef extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn( heroic )
  lazy val heroic = uri("git://github.com/softprops/heroic.git#926cc9ffd8300eb77987c49c56bc4118261e2552")
}
