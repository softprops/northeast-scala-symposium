package nescala.boston2015

import nescala.Store

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

  def get(id: String): Option[Member] =
    Store {
      _.hmget[String, String](
        key(id),
        "name", "photo", "mtime", "twttr").map { attrs =>
          Member(id, attrs("name"), attrs("photo"), attrs("mtime"),
                 attrs.get("twttr"))
        }
    }
}
