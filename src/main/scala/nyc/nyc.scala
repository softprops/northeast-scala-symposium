package nescala.nyc

import unfiltered.request._
import unfiltered.response._

import nescala.{ Cached, Config, Meetup }

object Nyc extends Config {
  import net.liftweb.json.compact
  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._

  def site: unfiltered.Cycle.Intent[Any, Any] = {      
    case GET(Path("/nyc/rsvps") & Jsonp.Optional(jsonp)) =>
      JsonContent ~> ResponseString(
        jsonp.wrap(Cached.getOr("meetup:event:%s:rsvps" format Meetup.Nyc.event_id) {
          (compact(render(Meetup.rsvps(Meetup.Nyc.event_id))), None)
        }))
    case GET(Path("/nyc/photos") & Jsonp.Optional(jsonp)) =>
      JsonContent ~> ResponseString(
        jsonp.wrap(Cached.getOr("meetup:event:%s" format Meetup.Nyc.event_id){
          (compact(render(Meetup.photos(Meetup.Nyc.event_id))), None)
        }))
  }
}
