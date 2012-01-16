package nescala.boston

import nescala.{ Cached, Clock, CookieToken, ClientToken,
                Meetup, Store }
import nescala.request.UrlDecoded

// panel proposals
object Panels {
  import unfiltered._
  import unfiltered.request._
  import unfiltered.response._
  import QParams._

  val MaxProposals = 3
  val MaxTalkName = 200
  val MaxTalkDesc = 600

  def errorJson(msg: String) = """{"status":400,"msg":"%s"}""" format msg

  def proposalJson(prop: String) = """{"status":200,"proposal":"%s"}""" format prop

  val withdrawing: Cycle.Intent[Any, Any] = {
     // delete
    case POST(Path("/boston/panel_proposals/withdraw")) &
      CookieToken(ClientToken(_, _, Some(_), Some(mid))) &
        Params(p) => Clock("withdrawing panel proposal") {

      val expected = for {
        id <- lookup("id") is required("id is required")
      } yield {
        val key = id.get
        val Withdrawing = """boston:panel_proposals:(.*):(.*)""".r
        (Store { s =>
          if(!s.exists(key))  Left("panel proposal did not exist")
          else {
            key match {
              case Withdrawing(who, id) =>
                if(mid.equals(who)) {
                  s.del(key).map( stat => if(stat > 0) s.decr(
                    "count:boston:panel_proposals:%s" format who
                  ))
                  Right(key)
                } else Left("not authorized to withdraw this panel proposal")
              case bk =>
                Left("invalid panel proposal")
            }
          }
        }).fold({ fail =>
          JsonContent ~> ResponseString(errorJson(fail))
        }, { ok =>
          JsonContent ~> ResponseString(proposalJson(ok))
        })
      }
      expected(p) orFail { errors =>
        JsonContent ~> ResponseString(errorJson(
          errors.map { _.error } mkString(". ")
        ))
      }
    }
  }

  val intent: Cycle.Intent[Any, Any] = {

    case POST(Path("/boston/panel_proposals")) &
      CookieToken(ClientToken(_, _, Some(_), Some(mid))) & Params(p) => Clock("creating boston panel proposal") {

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
            // count:{city}:panel_proposals:{memberId} stores the number of proposals a member submitted
            // {city}:panel_proposals:ids stores an atomicly incremented int used to generate proposals ids
            // {city}:panel_proposals:{memberId}:{nextId} stores a map of name, desc, and votes for a proposal
            val mkey = "boston:panel_proposals:%s" format mid
            val mukey = "boston:members:%s" format mid
            val ckey = "count:%s" format mkey
            val proposed = s.get(ckey).getOrElse("0").toInt
            if(proposed + 1 > MaxProposals) Left("Exceeded max panel proposals")
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

              val nextId = s.incr("boston:panel_proposals:ids").get
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
          JsonContent ~> ResponseString(errorJson(fail))
        },{ ok =>
          JsonContent ~> ResponseString("""{"status":200,"proposals":%s, "id":"%s"}""" format(ok._1, ok._2))
        })
      }
      expected(p) orFail { errors =>
        JsonContent ~> ResponseString(errorJson(
          errors.map { _.error } mkString(". ")
        ))
      }
    }

    case POST(Path(Seg("boston" :: "panel_proposals" :: UrlDecoded(id) :: Nil))) & Params(p) & CookieToken(
      ClientToken(_, _, Some(_), Some(mid))) => Clock("editing panel proposal %s" format id) {
      val expected = for {
        name <- lookup("name") is required("name is required")
        desc <- lookup("desc") is required("desc is required")
      } yield {
        val (n, d) = (name.get.trim, desc.get.trim)
        (if(n.size > MaxTalkName || d.size > MaxTalkDesc) Left("Talk contents were too long")
        else {
          val Pkey = """boston:panel_proposals:(.*):(.*)""".r
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
          JsonContent ~> ResponseString(errorJson(fail))
        }, { ok =>
          JsonContent ~> ResponseString("""{"status":200, "id":"%s"}""" format ok)
        })
      }
      expected(p) orFail { errors =>
        JsonContent ~> ResponseString(errorJson(
          errors.map { _.error } mkString(". ")
        ))
      }
    }

  }
}
