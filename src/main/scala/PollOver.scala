package nescala

import unfiltered.request._
import unfiltered.response._

object PollOver extends Templates {
  def intent: unfiltered.Cycle.Intent[Any, Any] = {
    case _ => page(
      <p>
        <h2>Sorry. Polls are closed.</h2>
        <h3>Results will soon be posted <a href="/">here</a></h3>
      </p>
    )
  }

  def page = layout(Nil)(Nil)_
}
