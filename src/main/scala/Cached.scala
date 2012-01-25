package nescala

object Cached extends Config {
  import scala.collection.JavaConversions._
  /** todo: heroku requires a sasl enabled memcached client
   *  tried a few options including java clients with no luck
   *  use https://github.com/softprops/mimsy when it's ready for
   *  prime time */
  def getOr(key: String)(f: => (String, Option[Int])): String = {
    val (value, _) = f
    value
  }
}
