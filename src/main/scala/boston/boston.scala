package com.meetup.boston

import com.meetup.{ CookieToken, ClientToken, PollOver, Tally }

object Boston extends Templates {
  import unfiltered.request._
  import unfiltered.response._

  def site: unfiltered.Cycle.Intent[Any, Any]  = {
    case GET(Path("/") & CookieToken(ClientToken(v, s, Some(c)))) =>
      indexWithAuth
    case GET(Path("/")) =>
      indexNoAuth
    case req @Path("/vote") => PollOver.intent(req)
    case req @Path("/tally") => Tally.intent(req)
  }
}
