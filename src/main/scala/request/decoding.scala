package nescala.request

object UrlDecoded {
  import java.net.URLDecoder.decode
  def unapply(raw: String) =
    Some(decode(raw, "utf8"))
}
