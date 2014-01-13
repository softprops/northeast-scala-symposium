package nescala.nyc2014

import nescala.{ Cached, Clock, AuthorizedToken, Meetup, Store }
import unfiltered.request._
import unfiltered.response._
import unfiltered.Cycle
import unfiltered.request.QParams._
import com.redis.RedisClient
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

object Nyc extends Templates {
  def site: unfiltered.Cycle.Intent[Any, Any] =
    index

  private def index: Cycle.Intent[Any, Any]  = {
    case GET(Path(Seg(Nil))) => Clock("home") {
      indexPage(false)
    }
  }
}
