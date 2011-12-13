package nescala

object Store extends Config {
  import com.redis._

  private lazy val (auth, clients) = {
    val URI = """^redis://(\w+):(\w+)@(.*):(\d{4}).*""".r
    val prop = property("REDISTOGO_URL")
    prop match {
      case URI(_, pass, host, port) =>
        (pass, new RedisClientPool(host, port.toInt))
      case mf =>
        sys.error("malformed redis uri: %s" format mf)
    }
  }

  def apply[T](f: RedisClient => T): T =
    clients.withClient { c =>
      // here we are attempting an authenticated
      // request. we will retry after trying
      // to reconnect, should a connection is dropped
      def attempt: T = try {
        c.auth(auth)
        f(c)
      } catch {
        case e: com.redis.RedisConnectionException =>
          println("redis fail!")
          if(c.reconnect) attempt
          else throw e
      }
      attempt
    }
}
