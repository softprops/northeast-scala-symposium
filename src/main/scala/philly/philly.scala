package nescala.philly

import nescala.{ Cached, Clock, AuthorizedToken, Meetup, Store }

object Philly extends Templates {
  import unfiltered.request._
  import unfiltered.response._
  import unfiltered.Cycle
  import QParams._
  import com.redis.RedisClient

  def site: unfiltered.Cycle.Intent[Any, Any] =
    (index /: Seq(talkProposals,
                  api,
                  Tally.talks))(_ orElse _)

  def mukey(of: String) = "philly:members:%s" format of

  private def index: Cycle.Intent[Any, Any]  = {
    case GET(Path(Seg(Nil))) => Clock("home") {
      Store { s =>
        indexPage(false, keynote(s), talks(s))
      }
    }
    case GET(Path(Seg("2013" :: "friends" :: Nil))) =>
      sponsors
  }

  private def api: unfiltered.Cycle.Intent[Any, Any] = {
    case GET(Path(Seg("philly" :: "rsvps" :: event :: Nil))) =>
      Clock("fetching rsvp list for %s" format event) {
        import net.liftweb.json._
        import net.liftweb.json.JsonDSL._
        JsonContent ~> ResponseString(
          Cached.getOr("meetup:event:%s:rsvps" format event) {
            (compact(render(Meetup.rsvps(event))), Some(60 * 15))
          })
      }
  }

  /** redirect home after we close this */
  private def talkProposals: Cycle.Intent[Any, Any] = 
    Proposals.viewing

  private def proposals(r: RedisClient, mid: String): Seq[Map[String, String]] = {
    r.keys("philly:proposals:%s:*" format mid) match {
      case None => Seq.empty[Map[String, String]]
      case Some(keys) =>
        (Seq.empty[Map[String, String]] /: keys.filter(_.isDefined))(
          (a, e) => a ++
          r.hmget[String, String](e.get, "name", "desc").map(_ + ("id" -> e.get))
        )
    }
  }

  private def talks(r: RedisClient): Seq[Map[String, String]] = {    
    val Talk = """philly:talks:(.*)""".r
    r.keys("philly:talks:*") match {
      case None => Seq.empty[Map[String, String]]
      case Some(keys) =>
        ((List.empty[Map[String, String]] /: keys.flatten)(
          (a, e) => (e match {
            case t @ Talk(mid) =>
              r.hmget[String, String](t, "name", "desc", "slides", "video").map(_ + ("id" -> t)).map {
                _ ++ (r.hmget[String, String](mukey(mid),
                                              "mu_name", "mu_photo", "twttr").get)
              }.get :: a
            case _ => a
          }))).reverse
    }
  }


  private def keynote(r: RedisClient): Map[String, String] = {
    val Keynote = """philly:keynote:(.*)""".r
    r.keys("philly:keynote:*") match {
      case None => Map.empty[String, String]
      case Some(Some(key) :: _) =>
        key match {
          case k @ Keynote(mid) =>
            r.hmget[String, String](k, "name", "desc", "slides", "video").map(_ + ("id" -> k)).map {
              _ ++ r.hmget[String, String](mukey(mid),
                                           "mu_name", "mu_photo", "twttr").flatten
            }.get
          case _ => Map.empty[String, String]
        }
      case _ => Map.empty[String, String]
    }
  }
}
