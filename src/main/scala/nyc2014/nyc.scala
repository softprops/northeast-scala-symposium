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

object Nyc extends Templates {

  def mukey(of: String) = s"nyc2014:members:$of"

  def site: unfiltered.Cycle.Intent[Any, Any] =
    (index orElse talkProposals)

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

  private def proposals(r: RedisClient, mid: String): Seq[Map[String, String]] = {
    r.keys(s"nyc2014:proposals:$mid:*") match {
      case None => Seq.empty[Map[String, String]]
      case Some(keys) =>
        (Seq.empty[Map[String, String]] /: keys.filter(_.isDefined))(
          (a, e) => a ++
          r.hmget[String, String](e.get, "name", "desc", "kind").map(_ + ("id" -> e.get))
        )
    }
  }
}
