package com.meetup

import unfiltered.Cookie
import unfiltered.request.{Cookies, HttpRequest}

case class ClientToken(value: String, sec: String, code: Option[String]) {
  def toCookieString = code match {
    case Some(c) => "%s!!!%s!!!%s" format(value, sec, c)
    case _ => "%s!!!%s" format(value, sec)
  }
}

object ClientToken {
  def fromCookieString(str: String) = str.split("!!!") match {
    case Array(v, s, c) => ClientToken(v, s, Some(c))
    case Array(v, s) => ClientToken(v, s, None)
    case _ => error("invalid token cookie string format %s" format str)
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
