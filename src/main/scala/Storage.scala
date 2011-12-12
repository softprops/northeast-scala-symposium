package nescala

object Store extends Config {
  import com.redis._

  /*private lazy val (auth, clients) = {
    val URI = """^redis://(\w+):(\w+)@(.*):(\d{4}).*""".r
    val prop = property("REDISTOGO_URL")
    prop match {
      case URI(_, pass, host, port) =>
        (pass, new RedisClientPool(host, port.toInt))
      case mf => sys.error("malformed redis uri: %s" format mf)
    }
  }*/
 
  def configured[T](f: RedisClient => T) = {
    val URI = """^redis://(\w+):(\w+)@(.*):(\d{4}).*""".r
    val prop = property("REDISTOGO_URL")
    prop match {
      case URI(_, pass, host, port) =>
        val c = new RedisClient(host, port.toInt)
        c.auth(pass)
        f(c)
      case mf => sys.error("malformed redis uri: %s" format mf)
    }
  }

  def apply[T](f: RedisClient => T) =
    configured(f)
    /*clients.withClient { c =>
      c.auth(auth)
      f(c)
    }*/
}
