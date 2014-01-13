package nescala.philly

import nescala.{ Cached, Clock, AuthorizedToken, Meetup, Store }
import unfiltered.request._
import unfiltered.response._
import unfiltered.Cycle
import unfiltered.request.QParams._
import com.redis.RedisClient
import org.json4s.native.JsonMethods._

object Philly extends Templates {
  def site: unfiltered.Cycle.Intent[Any, Any] =
    (index /: Seq(talkProposals,
                  api,
                  Tally.talks))(_ orElse _)

  def mukey(of: String) = "philly:members:%s" format of

  private def index: Cycle.Intent[Any, Any]  = {
    case GET(Path(Seg(/*"2013" :: */Nil))) => Clock("home") {
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
    case GET(Path(Seg("philly" :: "rsvps" :: event :: Nil))) => // fixme(doug): this path should be 2013
      Clock("fetching rsvp list for %s" format event) {
        JsonContent ~> ResponseString(
          compact(render(Cached.Philly.rsvps)))
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

  def switchSpeaker(current: Int, target: Int) =
    Store { s =>
      val talk = "2013:philly:talk:%s" format current
      val member = mukey(target.toString)
      if (!s.exists(talk)) Left("%s does not appear to be speaking" format current)
      else if (!s.exists(member)) Left("%s does not appear to be a member" format target)
      else {
        val talkAttrs = s.hmget[String, String](talk,
                                                "name",
                                                "desc",
                                                "slides",
                                                "video",
                                                "track",
                                                "order").get
        s.hmset("2013:philly:talk:%s" format target, talkAttrs)
        s.del(talk)
        Right(talkAttrs)
      }
    }

  def syncMember(mid: String) =
    Store { s =>
      Meetup.members(Seq(mid)).headOption match {
        case Some(mem) =>
          val key = mukey(mid)
          println("updating member %s" format key)
          println("name %s" format mem.name)
          println("photo %s" format mem.photo)
          s.hmset(key, Map(
            "mu_name" -> mem.name,
            "mu_photo" -> mem.photo
          ) ++ mem.twttr.map("twttr" -> _))
        case _ => () // not very likely
      }
   }

  def keynoteVideo(speaker: Int, url: String) =
    video(speaker, "keynote", url)

  def talkVideo(speaker: Int, url: String) =
    video(speaker, "talk", url)

  def video(speaker: Int, kind: String, url: String) =
    Store { s =>
      val key = "2013:philly:%s:%s" format(kind, speaker)
      if (!s.exists(key)) Left("%s does not appear to be speaking" format speaker)
      else {
        Right(s.hmset(key, Map("video" -> url)))
      }
    }
  
  def keynoteSlides(speaker: Int, url: String) =
    slides(speaker, "keynote", url)

  def talkSlides(speaker: Int, url: String) =
    slides(speaker, "talk", url)

  def slides(speaker: Int, kind: String, url: String) =
    Store { s =>
      val key = "2013:philly:%s:%s" format(kind, speaker)
      if (!s.exists(key)) Left("%s does not appear to be speaking" format speaker)
      else {
        Right(s.hmset(key, Map("slides" -> url)))
      }
    }

  def placeInTrack(member: Int, track: Int, order: Int) =
    Store { s =>
      val key = "2013:philly:talk:%s" format member
      if (!s.exists(key)) Left("%s does not appear to be speaking" format member)
      else {
        Right(s.hmset(key, Map(
          "track" -> track.toString,
          "order" -> order.toString
        )))
      }
    }

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
              r.hmget[String, String](t, "name", "desc", "slides", "video", "track", "order").map(_ + ("id" -> t)).map {
                _ ++ r.hmget[String, String](mukey(mid), "mu_name", "mu_photo", "twttr").get
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
              _ ++ r.hmget[String, String](mukey(mid), "mu_name", "mu_photo", "twttr").get
            }.get
          case _ => Map.empty[String, String]
        }
      case _ => Map.empty[String, String]
    }
  }
}
