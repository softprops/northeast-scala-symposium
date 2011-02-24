import sbt._

class Project(info: ProjectInfo) extends AppengineProject(info)
  with DataNucleus {

  val uf_version = "0.3.0"

  // unfiltered
  lazy val uff = "net.databinder" %% "unfiltered-filter" % uf_version
  lazy val ufj = "net.databinder" %% "unfiltered-jetty" % uf_version
  lazy val ufjs = "net.databinder" %% "unfiltered-json" % uf_version

  val dispatch_vers = "0.8.0.Beta4-SNAPSHOT"
  lazy val dispatch_meetup = "net.databinder" %% "dispatch-meetup" % dispatch_vers
  lazy val dispatch_gae = "net.databinder" %% "dispatch-http-gae" % dispatch_vers
  // persistence
  // val jdo = "javax.jdo" % "jdo2-api" % "2.3-ea"

  // testing
  lazy val uf_spec = "net.databinder" %% "unfiltered-spec" % uf_version % "test"
  lazy val jboss = "JBoss repository" at
    "https://repository.jboss.org/nexus/content/groups/public/"

  val appengineRepo = "nexus" at "http://maven-gae-plugin.googlecode.com/svn/repository/"
}
