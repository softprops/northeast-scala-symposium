package nescala

object Store extends Config {
  import com.redis._
  private lazy val (auth, clients) = {
    val URI = """^redis://(\w+):(\w+)@(.*):(\d{4}).*""".r
    val prop = property("REDISTOGO_URL")
    prop match {
      case URI(_, pass, host, port) =>
        (pass, new RedisClientPool(host, port.toInt))
      case mf => sys.error("malformed redis uri: %s" format mf)
    }
  }
 
  def apply[T](f: RedisClient => T) =
    clients.withClient { c =>
      c.auth(auth)
      f(c)
    }
}
