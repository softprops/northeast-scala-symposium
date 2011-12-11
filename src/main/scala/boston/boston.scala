package nescala.boston

import nescala.{ CookieToken, ClientToken, PollOver, Store, Tally }

object Boston extends Templates {
  import unfiltered.request._
  import unfiltered.response._
  import QParams._

  import net.liftweb.json._
  import net.liftweb.json.JsonDSL._


  def site: unfiltered.Cycle.Intent[Any, Any]  = {
    case GET(Path("/") & CookieToken(ClientToken(v, s, Some(c), Some(mid)))) =>
      indexWithAuth
    case GET(Path("/")) =>
      indexNoAuth
    case POST(Path("/boston/proposals")) & CookieToken(ClientToken(v, s, Some(c), Some(mid))) & Params(p) =>
      val expected = for {
        name <- lookup("name") is required("name is required")
        desc <- lookup("desc") is required("desc is required")
      } yield {
        val (n, d) = (name.get.trim, desc.get.trim)
        (Store { s =>
          // use instead, HINCRBY key field increment
          val proposed = s.get("proposals:%s:count" format mid).getOrElse("0").toInt
          if(proposed > 2) Left("Exceed max proposals")
          else {
            s.hmset("proposals:%s", Map(
              "name" -> n,
              "desc" -> d
            ))
            Right(s.incr("proposals:%s:count"))
          }
        }).fold({fail =>
          JsonContent ~> ResponseString("""{"status":400,"msg":"%s"}""" format fail)
        },{ count =>
          JsonContent ~> ResponseString("""{"status":200,"proposals":"%s"}""" format count)
        })
      }
      expected(p) orFail { errors =>
        JsonContent ~> ResponseString("""{"status":400,"msg":"%s"}""" format(
          errors.map { _.error } mkString(". ")
        ))
      }
    case req @Path("/vote") => PollOver.intent(req)
    case req @Path("/tally") => Tally.intent(req)
  }
}
