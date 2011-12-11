package com.meetup.boston

import com.meetup.{ PollOver, Tally }

object Boston extends Templates {
  import unfiltered.request._
  import unfiltered.response._

  def site: unfiltered.Cycle.Intent[Any, Any]  = {
    case GET(Path("/")) =>
      index
    case req @Path("/vote") => PollOver.intent(req)
    case req @Path("/tally") => Tally.intent(req)
  }
}
