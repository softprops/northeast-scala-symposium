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
          s.decr(s"count:$voter")
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
        if (!Meetup.rsvped(Meetup.Nyc2014.dayoneEventId, t.token)) JsonContent ~> ResponseString(
          errorJson("you must rsvp to vote")) else {
          val expected = for {
            vote <- lookup("vote") is required("vote is required")
            action <- lookup("action") is required("action is required")
          } yield {
            val Talk = """^nyc2014:proposals:(.*):(.*)$""".r
            val votedfor = vote.get
            JsonContent ~> (votedfor match {
              case Talk(member, talkId) =>
                Right((s"nyc2014:talk_votes:$mid",
                       s"count:nyc2014:talk_votes:$mid"))
              case _ =>
                Left("invalid kind of vote")
            }).fold({ err => ResponseString(errorJson(err)) }, { _ match {
              case (vkey, ckey) =>
                Store { s =>
                  val maxvotes = MaxTalkVotes
                  println(s"$vkey is attempting to vote for $votedfor count key $ckey")
                  if (!s.exists(votedfor)) ResponseString(errorJson("invalid vote")) else action match {
                    case Some("vote") =>
                      // member already voted for this
                      if (s.sismember(vkey, votedfor)) ResponseString(errorJson("vote exists"))
                      // member exceeded max votes
                      else if (s.get(ckey).map(_.toInt).filter(_ >= maxvotes).isDefined) ResponseString(
                        errorJson(s"max votes of $maxvotes exceeded")
                      ) else {
                        // capture vote
                        ResponseString(remainingJson(s.pipeline { pl =>
                          pl.hincrby(votedfor, "votes", 1)
                          pl.incr(ckey)
                          pl.sadd(vkey, votedfor)
                        }.map( _ match {
                          case totalVotes :: Some(currentCount) :: _ :: Nil =>
                            println(s"voted: $votedfor now has $totalVotes vote(s), $ckey has $currentCount votes")
                            maxvotes - currentCount.asInstanceOf[Long]
                          case _ =>
                            maxvotes
                        }).getOrElse(maxvotes)))
                      }
                    case Some("unvote") =>
                      ResponseString(remainingJson(s.pipeline { pl =>
                        pl.hincrby(votedfor, "votes", -1)
                        pl.decr(ckey)
                        pl.srem(vkey, votedfor)
                      }.map( _ match {
                        case totalVotes :: Some(currentCount) :: _ :: Nil =>
                          println(s"unvoted: $votedfor now has $totalVotes vote(s), $ckey has $currentCount votes")
                          maxvotes - currentCount.asInstanceOf[Long]
                        case _ => maxvotes
                      }).getOrElse(maxvotes)))

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
