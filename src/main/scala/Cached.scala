package nescala

import org.json4s._
import org.json4s.native.JsonMethods.parse
import scala.io.Source

object Cached extends Config {
  /** todo: heroku requires a sasl enabled memcached client
   *  tried a few options including java clients with no luck
   *  use https://github.com/softprops/mimsy when it's ready for
   *  prime time */
  def getOr(key: String)(f: => (String, Option[Int])): String = {
    val (value, _) = f
    value
  }

  private def read(city: String)(path: String) =  {
    val src = Source.fromURL(getClass().getResource(s"/cache/$city/$path"))
    val cached = parse(src.getLines().mkString(""))
    src.close
    cached
  }

  object Philly {
    lazy val rsvps = read("philly")("rsvps_97192402.json")
  }

  object Boston {
    val cached = read("boston")_
    val Rsvps = Map(
      "37637442" -> cached("rsvps_37637442.json"),
      "44049692" -> cached("rsvps_44049692.json"),
      "44042982" -> cached("rsvps_44042982.json")
    ).withDefaultValue(JArray(Nil))
  }

  object Nyc {
    val cached = read("nyc")_
    val rsvps = cached("rsvps_15526582.json")
    val photos = cached("photos_15526582.json")
  }
}
