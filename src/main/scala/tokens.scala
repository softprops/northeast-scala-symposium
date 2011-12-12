package nescala

import unfiltered.Cookie
import unfiltered.request.{Cookies, HttpRequest}

case class ClientToken(value: String, sec: String, code: Option[String], memberId: Option[String]) {
  def toCookieString = (code, memberId) match {
    case (Some(c), Some(m)) => "%s|%s|%s|%s" format(value, sec, c, m)
    case _ => "%s|%s" format(value, sec)
  }
}

object ClientToken {
  def fromCookieString(str: String) = str.split('|') match {
    case Array(v, s, c, m) => ClientToken(v, s, Some(c), Some(m))
    case Array(v, s) => ClientToken(v, s, None, None)
    case ary => sys.error("invalid token cookie string format %s %s" format(str, ary))
  }
}

object CookieToken {
  def unapply[T](r: HttpRequest[T]): Option[ClientToken] = r match {
    case Cookies(cookies) => cookies("token") match {
      case Some(Cookie(_, value, _, _, _, _)) =>
        Some(ClientToken.fromCookieString(value))
      case _ => None
    }
  }
  def apply[T](r: HttpRequest[T]) = unapply(r)
}
