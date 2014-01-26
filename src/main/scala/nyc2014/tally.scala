package nescala.nyc2014

import nescala.{ AuthorizedToken, Cached, Clock, Meetup, Store }
import unfiltered.Cycle
import unfiltered.request._
import unfiltered.response._

object Tally extends Templates {

  def talks: Cycle.Intent[Any, Any] = {
    case r @ GET(Path(Seg("2014" :: "tally" :: Nil))) =>
      r match {
        case AuthorizedToken(t) =>
          if (hosting(t.memberId.get)) talliedFor("proposals")
          else Redirect("/2014/talks")
        case _ =>
          Redirect("/2014/talks")
      }
  }

  private def talliedFor(kind: String) =
    Store { s =>
      val Talkr = s"""nyc2014:$kind:(.*):.*""".r
      val ks = s.keys(s"nyc2014:$kind:*:*").getOrElse(Nil).flatten
      val entries: Seq[Proposal] =
        (List.empty[Proposal] /: ks) {
          (a, e) =>
            s.hmget(e, "votes", "name", "kind").map(_ + ("id"-> e)).map {
              Proposal.fromMap(_) :: a
            }.getOrElse(a)
        }.map { talk =>
          talk.id match {
            case Talkr(who) =>
              val member = Member.fromMap(
                who,
                s.hmget(
                s"nyc2014:members:$who",
                "mu_name",
                "mu_photo",
                "mtime",
                "twittr").get)
              talk.copy(member = Some(member))
          }
        }
      val grouped = entries.groupBy(_.kind).map {
        case (kind, proposals) => (kind, proposals.sortBy(_.votes).reverse)
      }
      val total = entries.map(_.votes).sum
      tallied(true, total, grouped)
    }

  private def hosting(who: String) =
    Meetup.hosting(who, Meetup.Nyc2014.dayoneEventId)
}
