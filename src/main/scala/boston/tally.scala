package nescala.boston

import nescala.{ Cached, Clock, CookieToken,
                ClientToken, Meetup, Store }
object Tally extends Templates {
  import unfiltered._
  import unfiltered.request._
  import unfiltered.response._
  import QParams._

  def talks: Cycle.Intent[Any, Any] = {
    case GET(Path("/2012/talk_tally")) &
      CookieToken(ClientToken(_, _, Some(_), Some(mid))) =>      
      if(hosting(mid)) talliedFor("proposals")
      else Redirect("/2012/talks")
    case GET(Path("/2012/talk_tally")) => Redirect("/2012/talks")
  }

  def panels: Cycle.Intent[Any, Any] = {
    case GET(Path("/2012/panel_tally")) &
      CookieToken(ClientToken(_, _, Some(_), Some(mid))) =>      
      if(hosting(mid)) talliedFor("panel_proposals")
      else Redirect("/2012/panels")
    case GET(Path("/2012/panel_tally")) => Redirect("/2012/panels")
  }

  private def talliedFor(kind: String) =
    Store { s =>
      val Talkr = """boston:%s:(.*):.*""".format(kind).r
      val ks = s.keys("boston:%s:*:*".format(kind)).getOrElse(Nil).flatten
      val entries: Seq[Map[String, String]] = (List.empty[Map[String, String]] /: ks)((a,e) =>
        s.hmget(e, "votes", "name").get ++ Map("id"-> e) :: a
      ).map { talk =>
        talk("id") match {
          case Talkr(who) => talk ++ s.hmget(
            "boston:members:%s" format who, "mu_name", "mu_photo").get
        }
      }
      val total = entries.map(_("votes").toInt).sum
      tallied(total, entries.sortBy(_("votes").toInt).reverse)
    }

  private def hosting(who: String) = Meetup.hosting(who, Meetup.Boston.dayone_event_id)
}
