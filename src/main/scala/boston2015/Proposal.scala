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
  val Pattern = """boston2015:proposals:(.*):(.*)""".r
  val MaxTalkName = 200
  val MaxTalkDesc = 600
  val Max = 3

  def all: Iterable[Proposal] =
    Store { s =>
      s.keys(s"boston2015:proposals:*:*").getOrElse(Nil).flatMap {
        _.flatMap {
          case pkey @ Pattern(member, id) =>
            s.hmget[String, String](pkey, "name", "desc", "kind").map { attrs =>
              Proposal(pkey, attrs("name"), attrs("desc"), attrs("kind"), Member.get(member))
            }
        }
      }
    }

  def list(memberId: Int): Iterable[Proposal] =
    Store { s =>
      s.keys(s"boston2015:proposals:$memberId:*").getOrElse(Nil).flatMap {
        _.flatMap {
          pkey => s.hmget[String, String](pkey, "name", "desc", "kind").map { attrs =>
            Proposal(pkey, attrs("name"), attrs("desc"), attrs("kind"))
          }
        }
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
              .map( _ => s.decr(s"count:boston2015:proposals:$who")) match {
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
          val proposals = s"boston2015:proposals:${member.id}"
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
            val nextId = s.incr("boston2015:proposals:ids").get
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
