package nescala.boston2015

import nescala.{ Meetup, Store }
import com.redis.RedisClient

case class Member(
  id: String,
  name: String,
  photo: String,
  mtime: String,
  twttr: Option[String]) {

  def attrs = Map(
    "name" -> name,
    "photo" -> photo,
    "mtime" -> mtime
  ) ++ twttr.map("twttr" -> _)

  lazy val exists = Member.exists(this.id)
}

object Member {
  def key(memberId: String) =
    s"boston2015:members:${memberId}"

  def exists(memberId: String) =
    Store {
      _.exists(key(memberId))
    }

  def store(member: Member) =
    Store {
      _.hmset(
        key(member.id),
        member.attrs)
    }

  def sync(id: String): Option[Member] =
    Meetup.members(Seq(id)).headOption.map { m =>
      val fresh = Member(m.id, m.name, m.photo, System.currentTimeMillis.toString, m.twttr)
      store(fresh)
      fresh
    }

  def getOrFetch(id: String): Option[Member] =
    Store { s =>
      stored(id)(s) match {
        case None => Meetup.members(Seq(id))
          .headOption.map { m =>
            val fresh = Member(
              m.id, m.name, m.photo,
              System.currentTimeMillis.toString, m.twttr)
            store(fresh)
            fresh
          }
        case some => some
      }
    }

  def get(id: String): Option[Member] =
    Store(stored(id))

  def stored(id: String)(store: RedisClient) =
    store.hmget[String, String](
        key(id),
        "name", "photo", "mtime", "twttr").filter(_.nonEmpty).map { attrs =>
          Member(id, attrs("name"), attrs("photo"), attrs("mtime"),
                 attrs.get("twttr"))
        }

}
