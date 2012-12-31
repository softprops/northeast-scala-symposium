package nescala.boston

import nescala.{ AuthorizedToken, Cached, Clock, Meetup, Store }
import nescala.request.UrlDecoded

// talk proposals
object Proposals {
  import unfiltered._
  import unfiltered.request._
  import unfiltered.response._
  import QParams._

  val MaxProposals = 3
  val MaxTalkName = 200
  val MaxTalkDesc = 600

  def errorJson(msg: String) = """{"status":400,"msg":"%s"}""" format msg

  val withdrawing: Cycle.Intent[Any, Any] = {
    // delete
    case POST(Path("/boston/proposals/withdraw")) &
      AuthorizedToken(t) & Params(p) => Clock("withdrawing proposal") {
      val mid = t.memberId.get
      val expected = for {
        id <- lookup("id") is required("id is required")
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
          JsonContent ~> ResponseString(errorJson(fail))
        }, { ok =>
          JsonContent ~> ResponseString("""{"status":200,"pr
oposal":"%s"}""" format(ok))
        })
      }
      expected(p) orFail { errors =>
        JsonContent ~> ResponseString(errorJson(
          errors.map { _.error } mkString(". ")
        ))
      }
    }
  }

  val making: Cycle.Intent[Any, Any] = {
    // create
    case POST(Path("/boston/proposals")) &
      AuthorizedToken(t) & Params(p) => Clock("creating boston talk proposal") {
      val mid = t.memberId.get
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
            if(proposed + 1 > MaxProposals) Left("Exceeded max proposals")
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

    // edit
    case POST(Path(Seg("boston" :: "proposals" :: UrlDecoded(id) :: Nil))) & Params(p) &
      AuthorizedToken(t) => Clock("editing proposal %s" format id) {
      val mid = t.memberId.get
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
