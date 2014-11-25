package nescala

import dispatch._ // for future pimping
import dispatch.Defaults._
import unfiltered.request.{ Cookies, HttpRequest }
import unfiltered.response.{ ResponseFunction, SetCookies }
import unfiltered.Cookie
import scala.util.control.NonFatal

case class SessionCookie(session: Session, member: Int) {
  /** same semantics with Either#fold applied to the current session. if
   *  the current session is stale an attempt to refresh will be made. if session
   *  can not be refreshed the left value will be evaluated, otherwise the right function
   *  will be applied with a `fresh` cookie */
  def fold(
    left: => ResponseFunction[Any],
    right: SessionCookie => ResponseFunction[Any]): ResponseFunction[Any] =
    if (session.stale) (Meetup.refresh(session).map {
      refreshed => SessionCookie.drop(refreshed) ~> right(SessionCookie(refreshed, refreshed.memberId.apply()))
    }.recover {
      case NonFatal(_) =>
        SessionCookie.discard ~> left
    }).apply() else right(this)
}

object SessionCookie {
  val Name = "session"

  def drop(session: Session) =
    SetCookies(
      make(session, session.memberId.apply()))

  def discard =
    SetCookies.discarding(Name)

  def make(session: Session, memberId: Int) =
    Cookie(Name, str(session, memberId), httpOnly = true)

  def str(session: Session, memberId: Int) =
    Seq(session.uuid, memberId.toString, Hashing(session.uuid, memberId.toString))
      .mkString("|")

  def fromStr(str: String) =
    str.split('|') match {
      case Array(uuid, member, sig)
        if Hashing.authentic(sig, uuid, member) =>
        Session.get(uuid).map(SessionCookie(_, member.toInt))
      case _ =>
        None
    }

  object Value {
    def unapply(r: HttpRequest[_]): Option[SessionCookie] =
      r match {
        case Cookies(cookies) =>
          cookies(Name)
            .map(_.value)
            .flatMap(fromStr)
      }
  }
}
