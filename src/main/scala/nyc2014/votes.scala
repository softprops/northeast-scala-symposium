package nescala.nyc2014

import nescala.{ Cached, Clock, AuthorizedToken, Meetup, Store }
import nescala.request.UrlDecoded
import unfiltered._
import unfiltered.request._
import unfiltered.request.QParams._
import unfiltered.response._

// talk proposals
object Votes {

  val MaxTalkVotes = 8L

  def errorJson(msg: String) = """{"status":400, "msg":"%s"}""" format msg
  def remainingJson(rem: Long) = """{"status":200, "remaining":%d}""" format rem

  def voteCount(talkKey: String) =
    Store { s =>
      if (!s.exists(talkKey)) None
      else s.hmget(talkKey, "votes")
            .map(_("votes"))
    }

  def withdrawVotesFor(talkKey: String) =
    Store { s =>
      if (!s.exists(talkKey)) Nil
      else {
        // find voters
        val voters = s.keys("nyc2014:talk_votes:*")
                      .map(_.flatten.filter(s.sismember(_, talkKey)))
                      .getOrElse(Nil)
        // remove talk key from voters and decr counterKeys
        voters.map { voter =>
          s.srem(voter, talkKey)
          s.decr("count:%s" format voter)
          s.hincrby(talkKey, "votes", -1)
          voter
        }
      }
    }

  // member voting keys are stored in the format
  // nyc2014:proposals:{memberId}:{talkId}
  //
  // the members current voting count it stored in the format
  // count:nyc2014:talk_votes:{memberId}
  // 
  // when unvoting, given
  // - a talkKey: "nyc2014:proposals:{proposerId}:{talkId}"
  // - a voterCountKey "count:nyc2014:talk_votes:{voterId}"
  // - a voterVoteKey "nyc2014:talk_votes:{voterId}"
  // be sure to.
  //
  // 1) decrement the proposals vote count
  // redis.hincrby(talkKey, "votes", -1) 
  //
  // 2) decrement the voters voterCountKey
  // redis.decr(voterCountKey)
  //
  // 3) remove the proposal key from the votersvoterVoteKey
  // redis.srem(voterVoteKey, talkKey)

  def intent: unfiltered.Cycle.Intent[Any, Any] = {
    case POST(Path(Seg("2014" :: "votes" :: Nil))) &
      AuthorizedToken(t) & Params(p) => Clock("voting for proposal") {
        val mid = t.memberId.get
        if (!Meetup.has_rsvp(Meetup.Philly.eventId, t.token)) JsonContent ~> ResponseString(
          errorJson("you must rsvp to vote")) else {
          val expected = for {
            vote <- lookup("vote") is required("vote is required")
            action <- lookup("action") is required("action is required")            
          } yield {
            val Talk = """^nyc2014:proposals:(.*):(.*)$""".r
            val votedfor = vote.get
            JsonContent ~> (votedfor match {
              case Talk(member, talkId) =>
                Right(("nyc2014:talk_votes:%s" format mid,
                       "count:nyc2014:talk_votes:%s" format mid))
              case _ =>
                Left("invalid kind of vote")
            }).fold({ err => ResponseString(errorJson(err)) }, { _ match {
              case (vkey, ckey) =>
                Store { s =>
                  val maxvotes = MaxTalkVotes
                  if(!s.exists(votedfor)) ResponseString(errorJson("invalid vote"))
                  else action match {
                    case Some("vote") =>
                      if (s.sismember(vkey, votedfor)) ResponseString(errorJson("vote exists"))
                      else if (s.exists(ckey) && s.get(ckey).map(_.toInt).get >= maxvotes) ResponseString(
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
