package nescala

object Clock {
  import System.{currentTimeMillis => now }
  def apply[T](what: String)(f: => T): T = {
    val before = now
    try { f }
    finally {
      println("%s took %s ms" format(what, now - before))
    }
  }
}
