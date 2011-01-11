package com.meetup

import unfiltered.request._
import unfiltered.response._

/** Unfiltered plan */
class App extends unfiltered.filter.Plan {
  import QParams._

  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._

  def intent = {
    case GET(Path("/rsvps") & Jsonp.Optional(jsonp)) =>
      JsonContent ~> ResponseString(jsonp.wrap(compact(render(Meetup.rsvps))))

    case GET(Path("/event") & Jsonp.Optional(jsonp)) =>
      JsonContent ~> ResponseString(jsonp.wrap(compact(render(Meetup.event))))

    case req @ GET(Path("/poll")) => Poll.intent(req)

   //case GET(Path("/twttr", Jsonp.Optional(jsonp, _))) =>
   //   JsonContent ~> ResponseString(jsonp.wrap(compact(render(Twitter.tweets))))
  }
}
