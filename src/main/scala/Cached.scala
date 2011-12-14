package nescala

object Cached extends Config {
  import com.bitlove.memcached.pool._

  lazy val pool = {
    new MemcachedPool(property("MEMCACHE_SERVERS"))
  }

  def getOr(k: String)(f: => (String, Option[Int])): String = {
    val (value, ttl) = f
    value
  }
    
    /*pool { c =>
      c.get(k.getBytes("utf8")) match {
        case None =>
          val (value, expires) = f
          c.set(k.getBytes("utf8"), value.getBytes("utf8"), expires)
          value
        case Some(value) =>
          new String(value, "utf8")
      }
    }*/
}
