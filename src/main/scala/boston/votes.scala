package nescala.boston

import nescala.{ Cached, Clock, AuthorizedToken, Meetup, Store }
import nescala.request.UrlDecoded

// talk proposals
object Votes {
  import unfiltered._
  import unfiltered.request._
  import unfiltered.response._
  import QParams._

  val MaxTalkVotes = 8L
  val MaxPanelVotes = 1L

  def errorJson(msg: String) = """{"status":400, "msg":"%s"}""" format msg
  def remainingJson(rem: Long) = """{"status":200, "remaining":%d}""" format rem

  def intent: unfiltered.Cycle.Intent[Any, Any] = {
    case POST(Path(Seg("boston" :: "votes" :: Nil))) &
      AuthorizedToken(t) & Params(p) => Clock("voting for proposal") {
        val mid = t.memberId.get
        if(!Meetup.rsvped(Meetup.Boston.dayoneEventId, t.token)) JsonContent ~> ResponseString(
          errorJson("you must rsvp to vote")) else {
          val expected = for {
            vote <- lookup("vote") is required("vote is required")
            kind <- lookup("kind") is required("kind is required")
            action <- lookup("action") is required("action is required")
          } yield {
            val Talk = """^boston:proposals:(.*):(.*)$""".r
            val Panel = """^boston:panel_proposals:(.*):(.*)$""".r
            val votedfor = vote.get
            JsonContent ~> (kind match {
              case Some("talk") => votedfor match {
                case Talk(member, talkId) =>
                  Right(("boston:talk_votes:%s" format mid, "count:boston:talk_votes:%s" format mid))
                case _ => Left("invalid kind of vote")
              }
              case Some("panel") => votedfor match {
                case Panel(member, panelId) =>
                  Right(("boston:panel_votes:%s" format mid, "count:boston:panel_votes:%s" format mid))
                case _ => Left("invalid kind of vote")
              }
              case _ => Left("invalid kind of vote")
            }).fold({ err => ResponseString(errorJson(err)) }, { _ match {
              case (vkey, ckey) =>
                Store { s =>
                  val maxvotes = kind match { case Some("talk") => MaxTalkVotes case _ => MaxPanelVotes }
                  if(!s.exists(votedfor)) ResponseString(errorJson("invalid vote"))
                  else action match {
                    case Some("vote") =>
                      if(s.sismember(vkey, votedfor)) ResponseString(errorJson("vote exists"))
                      else if(s.exists(ckey) && s.get(ckey).map(_.toInt).get >= maxvotes) ResponseString(
                        errorJson("max votes of %d exceeded" format maxvotes)
                      ) else {
                        s.hincrby(votedfor, "votes", 1)
                        println("voted: %s now has %s vote(s), %s has (%s - 1) remaining" format(votedfor, s.hmget(votedfor, "votes"), ckey, s.get(ckey)))
                        s.sadd(vkey, votedfor)
                        ResponseString(remainingJson(
                          maxvotes - s.incr(ckey).get))
                      }
                    case Some("unvote") =>
                      s.hincrby(votedfor, "votes", -1)
                      println("unvoted: %s now has %s vote(s), %s has (%s + 1) remaining" format(votedfor, s.hmget(votedfor, "votes"), ckey, s.get(ckey)))
                      s.srem(vkey, votedfor)
                      ResponseString(remainingJson(
                        maxvotes - s.decr(ckey).get))
                    case _ =>
                      ResponseString(errorJson("invalid action"))
                  }
                }
            } })
          }
          expected(p) orFail { errors =>
            JsonContent ~> ResponseString(errorJson(
             errors.map { _.error } mkString(". ")))
          }
        }
      }
  }
}
