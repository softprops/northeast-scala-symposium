resolvers ++= Seq(
  "less is" at "http://repo.lessis.me",
  "coda" at "http://repo.codahale.com")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.3")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.1")
