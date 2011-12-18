package nescala.boston

import nescala.{ Cached, Clock, CookieToken, ClientToken,
                Meetup, PollOver, Store, Tally }

object Boston extends Templates {
  import unfiltered.request._
  import unfiltered.response._
  import QParams._

  val maxProposals = 3

  val MaxTalkName = 200
  val MaxTalkDesc = 600

  val Admins = Seq(8157820 /*doug*/,7230113 /*n8*/)

  object UrlDecoded {
     import java.net.URLDecoder.decode
    def unapply(raw: String) =
      Some(decode(raw, "utf8"))
  }

  def api: unfiltered.Cycle.Intent[Any, Any] = {
    case GET(Path(Seg("boston" :: "rsvps" :: event :: Nil))) => Clock("fetching rsvp list for %s" format event) {
      import net.liftweb.json._
      import net.liftweb.json.JsonDSL._

      JsonContent ~> ResponseString(
        Cached.getOr("meetup:event:%s:rsvps" format event) {
          (compact(render(Meetup.rsvps(event))), Some(60 * 15))
        })
    }

    case POST(Path(Seg("boston" :: "proposals" :: UrlDecoded(id) :: Nil))) & Params(p) & CookieToken(
      ClientToken(_, _, Some(_), Some(mid))) => Clock("editing proposal %s" format id) {
      val expected = for {
        name <- lookup("name") is required("name is required")
        desc <- lookup("desc") is required("desc is required")
      } yield {
        val (n, d) = (name.get.trim, desc.get.trim)
        (if(n.size > MaxTalkName || d.size > MaxTalkDesc) Left("Talk contents were too long")
        else {
          val Pkey = """boston:proposals:(.*):(.*)""".r
          id match {
            case Pkey(who, pid) =>
              if(!who.equals(mid)) Left("Invalid id")
              else {
                Store { s =>
                  if(!s.exists(id)) Left("Invalid proposal %s" format id)
                  else {
                    s.hset(id, "name", n)
                    s.hset(id, "desc", d)
                    Right(id)
                  }
                }
              }
           case invalid => Left("Invalid id")
          }
        }) fold({ fail =>
          JsonContent ~> ResponseString("""{"status":400,"msg":"%s"}""" format fail)
        }, { ok =>
          JsonContent ~> ResponseString("""{"status":200, "id":"%s"}""" format ok)
        })
      }
      expected(p) orFail { errors =>
        JsonContent ~> ResponseString("""{"status":400,"msg":"%s"}""" format(
          errors.map { _.error } mkString(". ")
        ))
      }
    }
  }

  def talks: unfiltered.Cycle.Intent[Any, Any] = {
    case GET(Path(Seg("2012" :: "talks" :: Nil))) => Clock("fetching 2012 talks") {
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

      def mukey(of: String) = "boston:members:%s" format of
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
      maybes(ret)
    }
  }

  def site: unfiltered.Cycle.Intent[Any, Any]  = {

    case GET(Path("/") & CookieToken(ClientToken(_, _, Some(_), Some(mid)))) =>
      val proposals = Store { s =>
        s.keys("boston:proposals:%s:*" format mid) match {
          case None => Seq.empty[Map[String, String]]
          case Some(keys) =>
            (Seq.empty[Map[String, String]] /: keys.filter(_.isDefined))(
              (a, e) => a ++
                s.hmget[String, String](e.get, "name", "desc").map(_ + ("id" -> e.get))
            )
        }
      }
      indexWithAuth(proposals)

    case GET(Path("/")) =>
      indexNoAuth

    case POST(Path("/boston/proposals/withdraw")) & CookieToken(ClientToken(_, _, Some(_), Some(mid))) & Params(p) => Clock("withdrawing proposal") {
      val expected = for {
        id <- lookup("id") is required("name is required")
      } yield {
        val key = id.get
        val Withdrawing = """boston:proposals:(.*):(.*)""".r
        (Store { s =>
          if(!s.exists(key))  Left("proposal did not exist")
          else {
            key match {
              case Withdrawing(who, id) =>
                if(mid.equals(who)) {
                  s.del(key).map( stat => if(stat > 0) s.decr(
                    "count:boston:proposals:%s" format who
                  ))
                  Right(key)
                } else Left("not authorized to withdraw this proposal")
              case bk =>
                Left("invalid proposal")
            }
          }
        }).fold({ fail =>
          JsonContent ~> ResponseString("""{"status":400,"msg":"%s"}""" format fail)
        }, { ok =>
          JsonContent ~> ResponseString("""{"status":200,"proposal":"%s"}""" format(ok))
        })
      }
      expected(p) orFail { errors =>
        JsonContent ~> ResponseString("""{"status":400,"msg":"%s"}""" format(
          errors.map { _.error } mkString(". ")
        ))
      }
    }


    case POST(Path("/boston/proposals")) & CookieToken(ClientToken(_, _, Some(_), Some(mid))) & Params(p) => Clock("fetching boston proposals") {
      val expected = for {
        name <- lookup("name") is required("name is required")
        desc <- lookup("desc") is required("desc is required")
      } yield {
        val (n, d) = (name.get.trim, desc.get.trim)
        (Store { s =>
          if(n.size > MaxTalkName || d.size > MaxTalkDesc) Left("Talk contents were too long")
          else {
            // `key` notes :)
            // talks are persisted as keys and values
            // count:{city}:proposals:{memberId} stores the number of proposals a member submitted
            // {city}:proposals:ids stores an atomicly incremented int used to generate proposals ids
            // {city}:proposals:{memberId}:{nextId} stores a map of name, desc, and votes for a proposal
            val mkey = "boston:proposals:%s" format mid
            val mukey = "boston:members:%s" format mid
            val ckey = "count:%s" format mkey
            val proposed = s.get(ckey).getOrElse("0").toInt
            if(proposed + 1 > maxProposals) Left("Exceeded max proposals")
            else {
              if(!s.exists(mukey)) {
                // cache meetup member data
                Meetup.members(Seq(mid)).headOption match {
                  case Some(mem) =>
                    s.hmset(mukey, Map(
                      "mu_name" -> mem.name,
                      "mu_photo" -> mem.photo
                    ) ++ mem.twttr.map("twttr" -> _))
                  case _ => () // not very likely
                }
              }

              val nextId = s.incr("boston:proposals:ids").get
              val nextKey = "%s:%s" format(mkey, nextId)
              s.hmset(nextKey, Map(
                "name" -> n,
                "desc" -> d,
                "votes" -> 0
              ))
              Right((s.incr(ckey).get, nextKey))
            }
          }
        }).fold({fail =>
          JsonContent ~> ResponseString("""{"status":400,"msg":"%s"}""" format fail)
        },{ ok =>
          JsonContent ~> ResponseString("""{"status":200,"proposals":%s, "id":"%s"}""" format(ok._1, ok._2))
        })
      }
      expected(p) orFail { errors =>
        JsonContent ~> ResponseString("""{"status":400,"msg":"%s"}""" format(
          errors.map { _.error } mkString(". ")
        ))
      }
    }
    //case req @Path("/vote") => PollOver.intent(req)
    //case req @Path("/tally") => Tally.intent(req)
  }
}
