package nescala.boston2015

import nescala.{ Store, Meetup }
import java.util.Date

case class Proposal(
  id: String,
  name: String,
  desc: String,
  kind: String,
  member: Option[Member] = None,
  time: Option[Date] = None) {
  lazy val domId = id.split(":")(3)
  lazy val memberId = id.split(":")(2) 
}

object Proposal {
  val Prefix = "boston2015:"
  val Pattern = s"""${Prefix}proposals:(.*):(.*)""".r
  val MaxTalkName = 200
  val MaxTalkDesc = 600
  val Max = 3
  val MaxVotes = 6

  def votekey(member: Int) = s"${Prefix}talk_votes:$member"

  def votes(member: Int): Set[String] =
    Store {
      _.smembers(votekey(member)).map(_.flatten).getOrElse(Set.empty)
    }

  def vote
   (member: Int,
    proposal: String,
    yes: Boolean): Either[String, Int] =
    proposal match {
      case Pattern(_, _) =>
        val votes = votekey(member)
        Store { s =>
          if (yes) {
            if (s.sismember(votes, proposal)) Left("vote exists")
            else if (s.scard(votes).map(_.toInt).exists(_ >= MaxVotes)) Left("max votes exceeded")
            else Right(s.pipeline { pl =>
              pl.hincrby(proposal, "votes", 1)
              pl.sadd(votes, proposal)
              pl.scard(votes)
            }.map {
              case pvotes :: _ :: Some(currentCount: Long) :: Nil =>
                MaxVotes - currentCount.toInt
              case _ =>
                MaxVotes
            }.getOrElse(MaxVotes))
          } else {
            if (!s.sismember(votes, proposal)) Left("vote not found") else Right(
              s.pipeline { pl =>
                pl.hincrby(proposal, "votes", -1)
                pl.srem(votes, proposal)
                pl.scard(votes)
              }.map {
                case pvotes :: _ :: Some(currentCount: Long) :: Nil =>
                  MaxVotes - currentCount.toInt
                case _ =>
                  MaxVotes
              }.getOrElse(MaxVotes))
          }
        }
      case _ =>
        Left("invalid vote")    
    }
    

  def all: Iterable[Proposal] =
    Store { s =>
      val grouped = s.keys(s"${Prefix}proposals:*:*").map(_.flatten).getOrElse(Nil)
       .groupBy {
         case Pattern(member, _) => member
       }
       val members = grouped.keys.map { member =>
         (member, Member.get(member))
       }.toMap
       (grouped.map {
         case (member, props) =>
           props.map { pkey =>
             val attrs = s.hmget[String, String](pkey, "name", "desc", "kind").get            
             Proposal(pkey, attrs("name"), attrs("desc"), attrs("kind"), members(member))
           }
       }).flatten
    }

  def list(memberId: Int): Iterable[Proposal] =
    Store { s =>
      s.keys(s"${Prefix}proposals:$memberId:*").map(_.flatten).getOrElse(Nil)
       .map { pkey =>
          val attrs = s.hmget[String, String](pkey, "name", "desc", "kind").get
          Proposal(pkey, attrs("name"), attrs("desc"), attrs("kind"))
        }
    }

  def trimmed(
    name: String, desc: String): Either[String, (String, String)] = {
    val (trimmedName, trimmedDesc) = (name.trim, desc.trim)
    if (trimmedName.size > MaxTalkName || trimmedDesc.size > MaxTalkDesc) Left("Talk contents were too long")
    else if (trimmedName.isEmpty || trimmedDesc.isEmpty) Left("Talk requires a name and description")
    else Right((trimmedName, trimmedDesc))
  }

  def withdraw(key: String) =
    Store { s =>
      key match {
        case Pattern(who, _) =>
          if (!s.exists(key)) Left("proposal does not exist") else {
            s.del(key).filter(_ > 0)
              .map( _ => s.decr(s"count:${Prefix}:proposals:$who")) match {
                case Some(value) =>
                  Right(value)
                case _ =>
                  Left("failed to withdraw proposal completely")
              }
          }
        case _ =>
          Left("invalid key")
      }
    }

  def edit(
    member: Member,
    key: String,
    name: String,
    desc: String,
    kind: String): Either[String, Proposal] =
    Store { s =>
      trimmed(name, desc).right.flatMap {
        case (trimmedName, trimmedDesc) =>
          key match {
            case Pattern(who, _) if who == member.id =>
              if (s.exists(key)) {
                s.hmset(key, Map(
                  "name" -> trimmedName,
                  "desc" -> trimmedDesc,
                  "kind" -> kind))
                Right(Proposal(key, trimmedName, trimmedDesc, kind, Some(member)))
              } else Left("Invalid proposal")
            case _ =>
              Left("Invalid proposal")
          }
      }
    }
  
  /** @return either of error or tuple of current proposal count for member and proposal */
  def create(
    member: Member,
    name: String,
    desc: String,
    kind: String): Either[String, (Long, Proposal)] =
    Store { s =>
      trimmed(name, desc).right.flatMap {
        case (trimmedName, trimmedDesc) =>
          val proposals = s"${Prefix}proposals:${member.id}"
          val counter = s"count:$proposals"
          val proposed = s.get(counter).getOrElse("0").toInt
          if (proposed + 1 > Max) Left("Exceeded max proposals") else {
            if (!member.exists) {
              println("[create] syncing member")
              Meetup.members(Seq(member.id)).headOption.foreach {
                case mem => Member.store(
                  Member(mem.id, mem.name, mem.photo,
                         System.currentTimeMillis.toString, mem.twttr))          
              }
            }
            val nextId = s.incr("${Prefix}proposals:ids").get
            val nextKey = s"$proposals:$nextId"
            s.hmset(nextKey, Map(
              "name"  -> trimmedName,
              "desc"  -> trimmedDesc,
              "kind"  -> kind,
              "votes" -> "0"
            ))
            Right((s.incr(counter).get,
                   Proposal(nextKey, trimmedName, trimmedDesc, kind, Some(member))))
          }
      }
    }
}
