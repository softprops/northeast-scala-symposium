package nescala

import dispatch._
import dispatch.oauth._
import com.ning.http.client.oauth.{ ConsumerKey, RequestToken }
import org.json4s._
import org.json4s.JsonDSL._
import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.ExecutionContext.Implicits.global

object Meetup extends Config {

  object Nyc {
    val event_id = property("nyc.event_id")
  }

  object Nyc2014 {
    val dayoneEventId = property("nyc.2014.dayone_event_id")
    val daytwoEventId = property("nyc.2014.daytwo_event_id")
  }

  object Boston {
    val dayone_event_id = property("boston.dayone_event_id")
    val daytwo_event_id = property("boston.daytwo_event_id")
    val daythree_event_id = property("boston.daythree_event_id")
  }

  object Philly {
    val eventId = property("philly.event_id")
  }

  case class SimpleMember(id: String, name: String, photo: String, twttr: Option[String])

  val DefaultImage =
    "http://img1.meetupstatic.com/39194172310009655/img/noPhoto_50.gif"

  lazy val consumer = new ConsumerKey(
    property("mu_consumer"), property("mu_consumer_secret"))  

  val AuthExchange = new dispatch.oauth.Exchange
    with SomeHttp with SomeConsumer with SomeEndpoints with SomeCallback {
    def http = Meetup.http
    lazy val consumer = Meetup.consumer
    val requestToken = "https://api.meetup.com/oauth/request"
    val accessToken = "https://api.meetup.com/oauth/access"
    val authorize = "http://www.meetup.com/authorize"
    val callback = "oob"

    def fetchRequestToken(callback: String)
      (implicit executor: ExecutionContext)
      : Future[Either[String,RequestToken]] = {
      val promised = http(
        url(requestToken) 
        << Map("oauth_callback" -> callback)
        <@ (consumer)
        > as.oauth.Token
      )
      for (eth <- message(promised, "request token")) yield eth.joinRight
    }
  }

  def host = :/("api.meetup.com").secure
  def apiKey = property("api_key")

  def http = Http

  def has_rsvp(eventId: String, token: RequestToken): Boolean = {
    val body = Clock("checking rsvp") {
      http(
        host / "2" / "event" / eventId <<? Map("fields" -> "self", "only" -> "self.rsvp.response") <@(consumer, token)
        > as.json4s.Json).apply()
    }
    (for (JString(resp) <- body \ "self" \ "rsvp" \ "response") yield resp)
    .headOption.filter(_ == "yes").isDefined
  }

  def members(ids: Traversable[String]): List[SimpleMember] = {
    val body = http(
      host / "2" / "members" <<? Map("key" -> apiKey, "member_id" -> ids.mkString(","))
      > as.json4s.Json).apply()
    for {
      JObject(fields) <- body
      ("results", JArray(ary))     <- fields
      JObject(member)              <- ary
      ("id", JInt(id))             <- member
      ("name", JString(name))      <- member
    } yield SimpleMember(
      id.toString,
      name,
      (for {
        ("photo", JObject(photo))     <- member
        ("photo_link", JString(link)) <- photo
      } yield link).headOption.getOrElse(DefaultImage),
      (for {
        ("other_services", JObject(srv)) <- member
        ("twitter", JObject(twt))        <- srv
        ("identifier", JString(twttr))   <- twt
      } yield twttr).headOption
    )
  }

  def member_id(token: RequestToken): Int = {
    val body = http(
      host / "2" / "member" / "self" <<? Map("only" -> "id") <@(consumer, token)
      > as.json4s.Json).apply()
    (for (JInt(id) <- body \ "id") yield id.toInt).headOption.getOrElse(0)
  }

  def photos(eventId: String) = {
    val body = http(
      host / "2" / "photos" <<? Map("key" -> apiKey, "event_id" -> eventId)
      > as.json4s.Json).apply()
    for {
      JObject(resp) <- body
      ("results", JArray(ary)) <- resp
      JObject(photo) <- ary
      ("photo_id", id) <- photo
      ("highres_link", hires) <- photo
      ("photo_link", link) <- photo
      ("thumb_link", thumb) <- photo
    } yield ("id" -> id) ~ ("hires_link" -> hires) ~ ("photo_link" -> link) ~ ("thumb_link" -> thumb)
  }

  def rsvps(eventId: String) = {
    def parse(res: JValue, meta: JValue): List[JValue] = {
      val yeses: List[(Int, String, String)] = for {
        JObject(rsvp)               <- res
        ("rsvp_id", JInt(id))       <- rsvp
        ("response", JString(resp)) <- rsvp
        if resp == "yes"
      } yield (
        id.toInt,
        (for {
          ("member", JObject(member)) <- rsvp
          ("name", JString(name))     <- member
        } yield name).headOption.getOrElse("???"),
        (for {
          ("member_photo", JObject(photo)) <- rsvp
          ("photo_link", JString(link))    <- photo
        } yield link).headOption.getOrElse(DefaultImage)
      )
      val json = yeses map {
        case (id, name, photo) =>
          ("id" -> id) ~ ("name" -> name) ~ ("photo" -> photo)
      }
      val JString(next) = meta \ "next"
      if (next.isEmpty) json else {
        val body = http(url(next) > as.json4s.Json).apply()
        val (r2, m2) = (for {
          JObject(fs)     <- body
          ("results", r2) <- fs
          ("meta", m2)    <- fs
        } yield (r2, m2)).head
        
        parse(r2, m2) ++ json
      }
    }
    val body = http(host / "2" / "rsvps" <<? Map("event_id" -> eventId, "key" -> apiKey)
                    > as.json4s.Json).apply()
    val (res, meta) = (for {
      JObject(fs)      <- body
      ("results", res) <- fs
      ("meta", meta)   <- fs
    } yield (res, meta)).head
    parse(res, meta)
  }

  def hosting(memberId: String, eventId: String) =
    hosts(eventId).contains(memberId.toInt)

  def hosts(eventId: String) =  {
    val body = http(host / "2" / "event" / eventId <<? Map(
      "fields" -> "event_hosts", "only" -> "event_hosts.member_id", "key" -> apiKey)
      > as.json4s.Json).apply()
    for (JInt(id) <- body \ "event_hosts" \ "member_id") yield id
  }

  def event(eventId: String) = {
    val body = http(host / "2"/ "event" / eventId <<? Map(
      "key" -> apiKey, "fields" -> "rsvp_rules", "only" -> "rsvp_rules,rsvp_limit,yes_rsvp_count")
      > as.json4s.Json).apply()
    for {
      JObject(event) <- body
      ("yes_rsvp_count", JInt(yes)) <- event
    } yield Map(
      "cutoff" -> (for (JInt(cutoff) <- body \ "rsvp_rules" \ "close_time") yield cutoff.toInt).headOption.getOrElse(0),
      "yes" -> yes.toInt,
      "no" -> 0,
      "limit" -> (for (JInt(lim) <- body \ "rsvp_limit") yield lim.toInt).headOption.getOrElse(0))
  }
}
