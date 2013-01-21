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
        indexPage(false, // authed
                  keynote(s),
                  scala.util.Random.shuffle(talks(s)))
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

  private def proposal(r: RedisClient, pkey: String) =
    r.hmget[String, String](pkey, "name", "desc")
     .map(_ + ("id" -> pkey))

  private def promote(pkey: String, to: String) = {
    val Prop = "philly:proposals:(.*):(.*)".r
    Store { s =>  
      proposal(s, pkey).map { prop =>
        prop("id") match {
          case Prop(mid, _) =>
            println("promoting %s'd proposal '%s' to %s"
                    .format(mid, prop("name"), to))
            Right(s.hmset("2013:philly:%s:%s".format(to, mid), Map(
              "name" -> prop("name"),
              "desc" -> prop("desc")
            )))
          case invalid =>
            Left("invalid proposal key %s" format invalid)
        }
      }.getOrElse(Left("could not find proposal"))
    }    
  }

  def promoteKeynote(pkey: String) =
    promote(pkey, "keynote")

  def promoteTalk(pkey: String) =
    promote(pkey, "talk")

  private def talks(r: RedisClient): Seq[Map[String, String]] = {    
    val Talk = """2013:philly:talk:(.*)""".r
    r.keys("2013:philly:talk:*") match {
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
    val Keynote = """2013:philly:keynote:(.*)""".r
    r.keys("2013:philly:keynote:*") match {
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
