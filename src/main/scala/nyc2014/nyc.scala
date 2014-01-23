package nescala.nyc2014

import nescala.{ Cached, Clock, AuthorizedToken, Meetup, Store }
import unfiltered.request._
import unfiltered.response._
import unfiltered.Cycle
import unfiltered.request.QParams._
import com.redis.RedisClient
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

object Member {
  def fromMap(id: String, data: Map[String, String]) =
    Member(id: String, data("mu_name"), data("mu_photo"), data("mtime"), data.get("twttr"))
}

case class Member(id: String, name: String, photo: String, mtime: String, twttr: Option[String]) {
  lazy val thumbPhoto = photo.replace("member_", "thumb_")
}

object Nyc extends Templates {

  def mukey(of: String) = s"nyc2014:members:$of"

  def site: unfiltered.Cycle.Intent[Any, Any] =
    (index orElse talkProposals orElse voting)

  private def index: Cycle.Intent[Any, Any]  = {
    case r @ GET(Path(Seg(Nil))) => Clock("home") {
      AuthorizedToken(r) match {
        case Some(t) =>
          println(s"logged in as ${t.memberId.get}")
          Store { s => indexPage(true, proposals = proposals(s, t.memberId.get)) }
        case _ =>
          println("alien")
          indexPage(false)          
      }      
    }
  }

  private def talkProposals: Cycle.Intent[Any, Any] = 
    (Proposals.creating orElse Proposals.editing orElse Proposals.viewing)

  private def voting: Cycle.Intent[Any, Any] =
    Votes.intent

  private def proposals(r: RedisClient, mid: String): Seq[Proposal] = {
    r.keys(s"nyc2014:proposals:$mid:*") match {
      case None => Seq.empty[Proposal]
      case Some(keys) =>
        (List.empty[Proposal] /: keys.filter(_.isDefined)) {
          (a, e) =>
            r.hmget[String, String](
              e.get, "name", "desc", "kind")
             .map(_ + ("id" -> e.get)).map {
               Proposal.fromMap(_) :: a
             }.getOrElse(a)
        }
    }
  }

  private def api: unfiltered.Cycle.Intent[Any, Any] = {
    case GET(Path(Seg("2014" :: "rsvps" :: event :: Nil))) =>
      Clock(s"fetching rsvp list for $event") {
        JsonContent ~> ResponseString(
          compact(render(Meetup.rsvps(event))))
      }
  }
}
