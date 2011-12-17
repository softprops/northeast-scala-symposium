package nescala

object Cached extends Config {
  import scala.collection.JavaConversions._

  /*import net.spy.memcached.{ConnectionFactory, ConnectionFactoryBuilder,
                            MemcachedClient}
  import net.spy.memcached.ConnectionFactoryBuilder.Protocol
  import net.spy.memcached.auth.{AuthDescriptor, PlainCallbackHandler}
  import java.net.InetSocketAddress

  def client = {
		val cf = new ConnectionFactoryBuilder()
      .setProtocol(Protocol.BINARY)
      .setAuthDescriptor(
        new AuthDescriptor(Array("PLAIN"),
          new PlainCallbackHandler(
            property("MEMCACHE_USERNAME"),
            property("MEMCACHE_PASSWORD")))).build()
		new MemcachedClient(cf, List(
      new InetSocketAddress(property("MEMCACHE_SERVERS"), 11211)))
  }*/

  def getOr(key: String)(f: => (String, Option[Int])): String = {
    val (value, _) = f
    value
    /*val cli = client
    cli.get(key) match {
      case null =>
        val (value, ttl) = f
        cli.add(key, ttl.getOrElse(0), value)
        value
      case value =>
        value.toString
    }*/
  }
}
