package nescala.boston2015

import dispatch._ // for future pimping
import dispatch.Defaults._
import nescala.{ Meetup, SessionCookie }
import unfiltered.request.{ Path, Seg }
import unfiltered.Cycle.Intent
import scala.util.control.NonFatal

object Site extends Templates {
  def pages: Intent[Any, Any] = {
    case req @ Path(Seg(Nil)) =>
      req match {
        case SessionCookie.Value(sc) =>
          sc.fold(indexPage(), { fresh => indexPage(Some(fresh)) })
        case _ =>
          indexPage()
      }
    case Path(Seg("2015" :: "proposals" :: Nil)) =>
      // todo
      indexPage()
  }
  
}
