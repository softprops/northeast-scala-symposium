package com.meetup.nyc

import unfiltered.request._
import unfiltered.response._

import unfiltered.Cookie
import dispatch.meetup.Auth
import dispatch.oauth.{ Consumer, Token }
import com.meetup.Meetup.Cities

import com.meetup.{ ClientToken, CookieToken, Config, Meetup, PollOver, Tally }

object Nyc extends Config {
  import net.liftweb.json.compact
  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._

  def site: unfiltered.Cycle.Intent[Any, Any] = {      
    case GET(Path("/nyc/rsvps") & Jsonp.Optional(jsonp)) =>
      JsonContent ~> ResponseString(
        jsonp.wrap(compact(render(Meetup.rsvps(Cities.nyc)))))
    case GET(Path("/nyc/photos") & Jsonp.Optional(jsonp)) =>
      JsonContent ~> ResponseString(
        jsonp.wrap(compact(render(Meetup.photos(Cities.nyc)))))
  }
}
