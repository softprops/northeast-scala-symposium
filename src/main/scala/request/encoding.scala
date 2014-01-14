package nescala.request

import java.net.{ URLDecoder, URLEncoder }

object UrlDecoded {
  def unapply(raw: String) =
    Some(URLDecoder.decode(raw, "utf8"))
}

object UrlEncoded {
  def apply(str: String) =
    URLEncoder.encode(str, "utf8")
}
