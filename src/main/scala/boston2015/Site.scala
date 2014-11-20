package nescala.boston2015

import unfiltered.request.{ Path, Seg }
import unfiltered.Cycle

object Site extends Templates {
  def pages: Cycle.Intent[Any, Any] = {
    case Path(Seg(Nil)) => indexPage
  }
}

