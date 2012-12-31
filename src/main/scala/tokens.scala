package nescala

import unfiltered.Cookie
import unfiltered.request.{Cookies, HttpRequest}
import dispatch.oauth.Token

case class ClientToken(value: String,
                       sec: String,
                       sig: String,
                       code: Option[String] = None,
                       memberId: Option[String] = None) {
  def toCookieString = (code, memberId) match {
    case (Some(c), Some(m)) =>
      Seq(value, sec, c, m, Hashing(value, sec, c, m))
        .mkString(ClientToken.Delimiter)
    case _ =>
      Seq(value, sec, Hashing(value, sec))
        .mkString(ClientToken.Delimiter)
  }

  def token = Token(value, sec)

  def authorized =
    code.isDefined && memberId.isDefined
}

object ClientToken {
  val Delimiter = "|"
  private val DelimiterChar = Delimiter(0)
  def fromCookieString(str: String) =
    str.split(DelimiterChar) match {
      case Array(v, s, c, m, sig)
        if (Hashing.authentic(sig, v, s, c, m)) =>
        Some(ClientToken(v, s, sig, Some(c), Some(m)))
      case Array(v, s, sig)
        if (Hashing.authentic(sig, v, s)) =>
          Some(ClientToken(v, s, sig))
      case _ => None
    }
}

object AuthorizedToken {
  def unapply[T](r: HttpRequest[T]): Option[ClientToken] =
    CookieToken(r).filter(_.authorized)
}

object CookieToken {
  def unapply[T](r: HttpRequest[T]): Option[ClientToken] =
    r match {
      case Cookies(cookies) =>
        cookies("token")
          .map(_.value)
          .flatMap(ClientToken.fromCookieString(_))
    }
  def apply[T](r: HttpRequest[T]) = unapply(r)
}
