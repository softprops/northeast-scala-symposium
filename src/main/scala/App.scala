package com.meetup

import unfiltered.request._
import unfiltered.response._

/** Unfiltered plan */
class App extends unfiltered.filter.Plan {
  import QParams._
  
  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._

  def intent = {
    case GET(Path("/rsvps", Params(p, _))) =>
      val rsvps = Meetup.rsvps
      val json = compact(render(rsvps map {
        case (id, name, photo) =>
          ("id" -> id) ~ ("name" -> name) ~ ("photo" -> photo)
      }))
      JsonContent ~> (p("callback") match {
        case Seq(cb) => ResponseString("%s(%s)" format(cb, json))
        case _ => ResponseString(json)
      })
  }
}
