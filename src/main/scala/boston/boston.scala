package nescala.boston

import nescala.{ Cached, Clock, CookieToken, ClientToken,
                Meetup, Store }

object Boston extends Templates {
  import unfiltered.request._
  import unfiltered.response._
  import unfiltered.Cycle
  import QParams._
  import com.redis.RedisClient

  private def mukey(of: String) = "boston:members:%s" format of

  def api: unfiltered.Cycle.Intent[Any, Any] = {
    case GET(Path(Seg("boston" :: "rsvps" :: event :: Nil))) => Clock("fetching rsvp list for %s" format event) {
      import net.liftweb.json._
      import net.liftweb.json.JsonDSL._

      JsonContent ~> ResponseString(
        Cached.getOr("meetup:event:%s:rsvps" format event) {
          (compact(render(Meetup.rsvps(event))), Some(60 * 15))
        })
    }
  }

  def panelProposals: Cycle.Intent[Any, Any] = {
    case req @ GET(Path(Seg("2012" :: "panels" :: Nil))) => Redirect("/") /*Clock("fetching 2012 panels") {
      val proposals = Store { s =>
        s.keys("boston:panel_proposals:*:*") match {
          case None => Seq.empty[Map[String, String]]
          case Some(keys) =>
            (Seq.empty[Map[String, String]] /: keys.filter(_.isDefined)){
              (a, e) => a ++
                s.hmget[String, String](e.get, "name", "desc").map(_ + ("id" -> e.get))
            }
        }
      }
      val Proposing = """boston:panel_proposals:(.*):(.*)""".r
      val pids = (proposals.map {
        _("id") match {
          case Proposing(who, _) =>
            who
        }
      }).toSet.toSeq
      
      // find the members we may not have cached yet
      val notcached = Store { s =>
        pids.filterNot(p => s.exists(mukey(p)))
      }
      val members = if(!notcached.isEmpty) {
        // query meetup for members
        // we don't have cached yet
        val ms = Meetup.members(notcached)
        Store { s =>
          ms.map { m =>
            val data = Map(
              "mu_name" -> m.name, "mu_photo" -> m.photo
            ) ++  m.twttr.map("twttr" -> _)
            s.hmset(mukey(m.id), data)
            m.id -> data
          }
        }
      } else {
        // load up cached members
        Store { s =>
          pids.map { p =>
            p -> s.hmget[String, String](mukey(p), "mu_name", "mu_photo", "twttr").get
          }
        }
      }
      val ret = (proposals /: members)((a, e) => e match {
        case (key, value) =>          
          val (matching, notmatching) = a.partition(_("id").matches("""boston:panel_proposals:%s:(.*)""".format(key)))
          matching.map(_ ++ value) ++ notmatching
      })

      val (can, votes) = req match {
        case CookieToken(ClientToken(token, sec, Some(_), Some(mid))) =>
          if(Meetup.has_rsvp(Meetup.Boston.dayone_event_id, dispatch.oauth.Token(token, sec))) {
            (true, Store {
              _.smembers("boston:panel_votes:%s" format mid).map(_.filter(_.isDefined).map(_.get).toSeq).getOrElse(Nil)
            })
          } else (false, Nil)
        case _ => (false, Nil)
      }
      panelListing(scala.util.Random.shuffle(ret), authed = can, votes = votes)
    }*/
  }

  def talkProposals: Cycle.Intent[Any, Any] = {
    case req @ GET(Path(Seg("2012" :: "talks" :: Nil))) => Redirect("/")/*Clock("fetching 2012 talks") {
      val proposals = Store { s =>
        s.keys("boston:proposals:*:*") match {
          case None => Seq.empty[Map[String, String]]
          case Some(keys) =>
            (Seq.empty[Map[String, String]] /: keys.filter(_.isDefined)){
              (a, e) => a ++
                s.hmget[String, String](e.get, "name", "desc").map(_ + ("id" -> e.get))
            }
        }
      }
      val Proposing = """boston:proposals:(.*):(.*)""".r
      val pids = (proposals.map {
        _("id") match {
          case Proposing(who, _) =>
            who
        }
      }).toSet.toSeq

      val notcached = Store { s =>
        pids.filterNot(p => s.exists(mukey(p)))
      }
      val members = if(!notcached.isEmpty) {
        val ms = Meetup.members(notcached)
        Store { s =>
          ms.map { m =>
            val data = Map(
              "mu_name" -> m.name, "mu_photo" -> m.photo
            ) ++  m.twttr.map("twttr" -> _)
            s.hmset(mukey(m.id), data)
            m.id -> data
          }
        }
      } else {
        Store { s =>
          pids.map { p =>
            p -> s.hmget[String, String](mukey(p), "mu_name", "mu_photo", "twttr").get
          }
        }
      }
      val ret = (proposals /: members)((a, e) => e match {
        case (key, value) =>          
          val (matching, notmatching) = a.partition(_("id").matches("""boston:proposals:%s:(.*)""".format(key)))
          matching.map(_ ++ value) ++ notmatching
      })
      val (can, votes) =  req match {
        case CookieToken(ClientToken(token, sec, Some(_), Some(mid))) =>
          if(Meetup.has_rsvp(Meetup.Boston.dayone_event_id, dispatch.oauth.Token(token, sec))) {
            (true, Store {
              _.smembers("boston:talk_votes:%s" format mid).map(_.filter(_.isDefined).map(_.get).toSeq).getOrElse(Nil)
            })
          } else (false, Nil)
        case _ => (false, Nil)
      }
      talkListing(scala.util.Random.shuffle(ret), authed = can, votes = votes)
    }*/
  }

  private def talks(r: RedisClient): Seq[Map[String, String]] = {    
    val Talk = """boston:talks:(.*)""".r
    r.keys("boston:talks:*") match {
      case None => Seq.empty[Map[String, String]]
      case Some(keys) =>
        ((List.empty[Map[String, String]] /: keys.flatten)(
          (a, e) => (e match {
            case t @ Talk(mid) =>
              r.hmget[String, String](t, "name", "desc", "slides").map(_ + ("id" -> t)).map {
                _ ++ (r.hmget[String, String](mukey(mid),
                                              "mu_name", "mu_photo", "twttr").get)
              }.get :: a
            case _ => a
          }))).reverse
    }
  }

  private def keynote(r: RedisClient): Map[String, String] = {
    val Keynote = """boston:keynote:(.*)""".r
    r.keys("boston:keynote:*") match {
      case None => Map.empty[String, String]
      case Some(Some(key) :: _) =>
        key match {
          case k @ Keynote(mid) =>
            r.hmget[String, String](k, "name", "desc").map(_ + ("id" -> k)).map {
              _ ++ r.hmget[String, String](mukey(mid),
                                           "mu_name", "mu_photo", "twttr").flatten
            }.get
          case _ => Map.empty[String, String]
        }
      case _ => Map.empty[String, String]
    }
  }

  private def panel(r: RedisClient): Map[String, String] = {
    val Keynote = """boston:panel:(.*)""".r
    r.keys("boston:panel:*") match {
      case None => Map.empty[String, String]
      case Some(skey :: _) =>
        skey match {
          case Some(key) =>
            key match {
              case k @ Keynote(mid) =>
                r.hmget[String, String](k, "name", "desc").map(_ + ("id" -> k)).map {
                  _ ++ r.hmget[String, String](mukey(mid),
                                           "mu_name", "mu_photo", "twttr").flatten
                }.get
              case _ =>
                Map.empty[String, String]
            }
          case _ =>
            Map.empty[String, String]
        }
      case _ => Map.empty[String, String]
    }
  }

  def site: Cycle.Intent[Any, Any]  = {
    case GET(Path("/") & CookieToken(ClientToken(_, _, Some(_), Some(mid)))) => Clock("home") {
      Store { s =>
        index(true, keynote(s), talks(s), panel(s))
      }
    }
    case GET(Path("/")) => Clock("home") {
      Store { s =>
        index(false, keynote(s), talks(s), panel(s))
      }
    }
    case GET(Path(Seg("2012" :: "friends" :: Nil))) =>
      sponsors
  }
}
