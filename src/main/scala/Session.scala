package nescala

import java.util.UUID
import dispatch._
import dispatch.Defaults._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.util.control.NonFatal

case class Session
 (uuid: String, access: String, refresh: String) {
  lazy val memberId: Future[Int] = Meetup.memberId(this)
  def stale = try Await.result(memberId, 3.seconds) < 1 catch {
    case NonFatal(_) => true
  }
}

object Session {

  def create(access: String, refresh: String): Session =
    Store { store =>
      val uuid = UUID.randomUUID.toString
      store.hmset(key(uuid), Map(
        "access"  -> access,
        "refresh" -> refresh))
      Session(uuid, access, refresh)
    }

  def get(uuid: String): Option[Session] =
    Store { store =>
      store.hmget[String, String](
        key(uuid), "access", "refresh").map { data =>
        Session(uuid, data("access"), data("refresh"))
      }
    }

  def delete(uuid: String) =
    Store { store =>
      store.del(key(uuid))
    }

  private def key(uuid: String) = s"nescala:sessions:$uuid"
}
