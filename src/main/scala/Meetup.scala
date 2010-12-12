package com.meetup

trait Cached {
  import com.google.appengine.api.memcache.{Expiration, MemcacheServiceFactory,MemcacheService}
 
  case class Cache(val svc: MemcacheService) {
    def isDefinedAt(k: String) = svc.contains(k)
    /** conditional get. */
    def getOr[T](k: String)(f: => (T, Option[Long])): T =
      (svc.get(k) match {
        case null =>
          val (value, expires) = f;
          expires match {
            case None => svc.put(k, value)
            case Some(exp) => svc.put(k, value, Expiration.onDate(new java.util.Date(exp)))
          }
          value
        case value => value
      }).asInstanceOf[T]

    def - (k: String) = svc.delete(k)
  }

  def cache(name: String) = Cache(MemcacheServiceFactory.getMemcacheService(name))
}

trait Config {
  private lazy val props = {
    val file = getClass.getResourceAsStream("/meetup.properties")
    val props = new java.util.Properties
    props.load(file)
    file.close()
    props
  }
  def property(name: String) = props.getProperty(name).toString
}

object Meetup extends Cached with Config {
  import dispatch._
  import meetup._
  import dispatch.liftjson.Js._
  import oauth._
  import Http._
  
  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._

  val event_id = property("event_id")
  val client: Client = APIKeyClient(property("api_key"))
  implicit def http = new dispatch.AppEngineHttp

  val rsvpCache = cache("rsvps")

  def rsvps = {
    import net.liftweb.json.JsonParser._
    val json = rsvpCache.getOr("rsvps")({
      val (res, meta) = client.call(Rsvps.event_id(event_id))
      val defaultImage = "http://img1.meetupstatic.com/39194172310009655/img/noPhoto_50.gif"
      val result = 
        for {
          r <- res
          id <- Rsvp.id(r)
          name <- Rsvp.name(r)
          photo <- Rsvp.photo_url(r)
          response <- Rsvp.response(r)
          if(response == "yes")
        } yield {
          (id, name, if(photo.isEmpty) defaultImage else photo)
        }
      (compact(render(result map {
        case (id, name, photo) =>
          ("id" -> id) ~ ("name" -> name) ~ ("photo" -> photo)
      })), Some(System.currentTimeMillis + 1000 * 60 * 2))
    })
    parse(json)
  }
}
