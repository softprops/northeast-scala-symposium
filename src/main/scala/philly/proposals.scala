package nescala.philly

import nescala.{ AuthorizedToken, Cached, Clock, Meetup, Store }
import nescala.request.UrlDecoded

// talk proposals
object Proposals extends Templates {
  import unfiltered._
  import unfiltered.request._
  import unfiltered.response._
  import QParams._

  val TalkTime = 30
  val MaxProposals = 3
  val MaxTalkName = 200
  val MaxTalkDesc = 600

  def errorJson(msg: String) = """{"status":400,"msg":"%s"}""" format msg

  def withdraw(mid: String, key: String) = {
    Votes.withdrawVotesFor(key) match {
      case Nil    => println("talk %s had no votes" format key)
      case votes  => println("remove votes %s for talk %s" format(votes, key))
    }
    val Withdrawing = """philly:proposals:(.*):(.*)""".r
    (Store { s =>
      if(!s.exists(key)) Left("proposal did not exist")
      else {
        key match {
          case Withdrawing(who, id) =>
            if(mid.equals(who)) {
              s.del(key).map( stat => if(stat > 0) s.decr(
                "count:philly:proposals:%s" format who
              ))
              Right(key)
            } else Left("not authorized to withdraw this proposal")
          case bk =>
            Left("invalid proposal")
        }
      }
   })
  }

  def create(mid: String, name: String, desc: String) = {
    val (n, d) = (name.trim, desc.trim)
    Store { s =>
      if(n.size > MaxTalkName || d.size > MaxTalkDesc) Left("Talk contents were too long")
      else {
        // `key` notes :)
        // talks are persisted as keys and values
        // count:{city}:proposals:{memberId} stores the number of proposals a member submitted
        // {city}:proposals:ids stores an atomicly incremented int used to generate proposals ids
        // {city}:proposals:{memberId}:{nextId} stores a map of name, desc, and votes for a proposal
        val mkey = "philly:proposals:%s" format mid
        val mukey = "philly:members:%s" format mid
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

          val nextId = s.incr("philly:proposals:ids").get
          val nextKey = "%s:%s" format(mkey, nextId)
          s.hmset(nextKey, Map(
            "name" -> n,
            "desc" -> d,
            "votes" -> 0
          ))
          Right((s.incr(ckey).get, nextKey))
        }
      }
    }
  }

  def currentProposals = {
    val proposals = Store { s =>
      s.keys("philly:proposals:*:*") match {
        case None => Seq.empty[Map[String, String]]
        case Some(keys) =>
          (Seq.empty[Map[String, String]] /: keys.filter(_.isDefined)){
            (a, e) => a ++
              s.hmget[String, String](e.get,
                                      "name",
                                      "desc")
                .map(_ + ("id" -> e.get))
          }
      }
   }

   val Proposing = """philly:proposals:(.*):(.*)""".r
   val pids = (proposals.map {
     _("id") match {
       case Proposing(who, _) =>
         who
     }
   }).toSet.toSeq

    val notcached = Store { s =>
      pids.filterNot(p => s.exists(Philly.mukey(p)))
    }

    val members = if (!notcached.isEmpty) {
      val ms = Meetup.members(notcached)
      Store { s =>
        ms.map { m =>
          val data = Map(
            "mu_name" -> m.name,
            "mu_photo" -> m.photo
          ) ++ m.twttr.map("twttr" -> _)
                s.hmset(Philly.mukey(m.id), data)
                m.id -> data
        }
      }
    } else {
      Store { s =>
        pids.map { p =>
          p -> s.hmget[String, String](
            Philly.mukey(p),
            "mu_name",
            "mu_photo",
            "twttr").get
        }
      }
    }

    (proposals /: members)((a, e) => e match {
      case (key, value) =>          
        val (matching, notmatching) = a.partition(_("id").matches("""philly:proposals:%s:(.*)""".format(key)))
        matching.map(_ ++ value) ++ notmatching
    })
  }

  val viewing: Cycle.Intent[Any, Any] = {
    case req @ GET(Path(Seg("2013" :: "talks" :: Nil))) => Clock("fetching 2012 talks proposals") {
      val (can, votes) =  req match {
        case AuthorizedToken(t)
          if (Meetup.has_rsvp(Meetup.Philly.eventId, t.token)) =>
            val mid = t.memberId.get
            (true, Store {
              _.smembers("philly:talk_votes:%s" format mid)
               .map(_.filter(_.isDefined).map(_.get).toSeq)
               .getOrElse(Nil)
            })
        case _ =>
          (false, Nil)
      }
      talkListing(scala.util.Random.shuffle(currentProposals),
                  canVote = can,
                  votes = votes)
    }
  }

  val making: Cycle.Intent[Any, Any] = {
    case POST(Path("/philly/proposals")) &
      AuthorizedToken(t) & Params(p) => Clock("creating philly talk proposal") {
      val expected = for {
        name <- lookup("name") is required("name is required")
        desc <- lookup("desc") is required("desc is required")
      } yield {
        create(t.memberId.get, name.get, desc.get)
          .fold({fail =>
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
    case POST(Path(Seg("philly" :: "proposals" :: UrlDecoded(id) :: Nil))) & Params(p) &
      AuthorizedToken(t) => Clock("editing proposal %s" format id) {
      val mid = t.memberId.get
      val expected = for {
        name <- lookup("name") is required("name is required")
        desc <- lookup("desc") is required("desc is required")
      } yield {
        val (n, d) = (name.get.trim, desc.get.trim)
        (if (n.size > MaxTalkName || d.size > MaxTalkDesc) Left("Talk contents were too long")
        else {
          val Pkey = """philly:proposals:(.*):(.*)""".r
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
