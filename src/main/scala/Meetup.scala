package nescala

import dispatch._
import dispatch.oauth._
import com.ning.http.client.oauth.{ ConsumerKey, RequestToken }
import org.json4s._
import org.json4s.JsonDSL._
import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

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

  lazy val consumerRedirectUri =
    property("mu_redirect_uri")

  def authorize(callback: String, state: Option[String] = None): String =
    s"https://secure.meetup.com/oauth2/authorize?scope=ageless&client_id=${consumer.getKey}&response_type=code&redirect_uri=$callback&state=${state.getOrElse("")}"

  def sign(req: Req, session: Session) =
    req <:< Map("Authorization" -> s"Bearer ${session.access}")

  def memberId(session: Session): Future[Int] =
    http(sign(url("https://api.meetup.com/2/member/self"), session)
         <<? Map("only" -> "id") OK as.json4s.Json)
      .map( js => (for {
        JObject(member)  <- js
        ("id", JInt(id)) <- member
      } yield id.toInt).head)

  def memberOf(session: Session, group: Int): Boolean =
    http(sign(url(s"https://api.meetup.com/2/profile/$group/self"), session)
         <<? Map("only" -> "id") OK as.String)
      .map(_ => true).recover {
        case NonFatal(_) =>
          false
      }.apply()

  def rsvped(session: Session, eventId: Int): Future[Boolean] =
    http(sign(url(s"https://api.meetup.com/2/event/$eventId"), session)
         <<? Map("fields" -> "self", "only" -> "self.rsvp.response")
         OK as.json4s.Json)
      .map { js =>
        (for {
          JObject(event)              <- js
          ("self", JObject(self))     <- event
          ("rsvp", JObject(rsvp))     <- self
          ("response", JString(resp)) <- rsvp
        } yield resp)
        .headOption
        .exists(_ == "yes")
      }.recover {
        case NonFatal(_) =>
          false
      }

  private def tokens(js: JValue): Option[(String, String)] =
    (for {
      JObject(response)                   <- js
      ("access_token", JString(access))   <- response
      ("refresh_token", JString(refresh)) <- response
    } yield (access, refresh)).headOption

  def access(code: String, redirect: String): Option[(String, String)] =
    http(url("https://secure.meetup.com/oauth2/access")
         << Map("client_id"     -> consumer.getKey,
                "client_secret" -> consumer.getSecret,
                "grant_type"    -> "authorization_code",
                "code"          -> code,
                "redirect_uri"  -> redirect) OK as.json4s.Json)
        .map(tokens).apply()
  
  def refresh(session: Session): Future[Session] =
    http(url("https://secure.meetup.com/oauth2/access")
         << Map("client_id"     -> consumer.getKey,
                "client_secret" -> consumer.getSecret,
                "grant_type"    -> "refresh_token",
                "refresh_token" -> session.refresh) OK as.json4s.Json)
      .map(tokens).map {
        case Some((access, refresh)) =>
          Session.delete(session.uuid)
          Session.create(access, refresh)
      }

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

  def http = new Http

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
    val body = Clock("fetching members") {
      http(
        host / "2" / "members" <<? Map("key" -> apiKey, "member_id" -> ids.mkString(","))
        > as.json4s.Json).apply()
    }
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
    val body = Clock("fetching member") {
      http(
        host / "2" / "member" / "self" <<? Map("only" -> "id") <@(consumer, token)
        > as.json4s.Json).apply()
    }
    (for (JInt(id) <- body \ "id") yield id.toInt).headOption.getOrElse(0)
  }

  def photos(eventId: String) = {
    val body = Clock("fetching photos") {
      http(
        host / "2" / "photos" <<? Map("key" -> apiKey, "event_id" -> eventId)
        > as.json4s.Json).apply()
    }
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
