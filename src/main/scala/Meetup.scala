package nescala

object Meetup extends Config {
  import dispatch._
  import meetup._
  import dispatch.liftjson.Js._
  import oauth._
  import Http._

  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.JsonParser._

  val DefaultImage = "http://img1.meetupstatic.com/39194172310009655/img/noPhoto_50.gif"

  lazy val consumer = Consumer(
    property("mu_consumer"), property("mu_consumer_secret"))

  object Nyc {
    val event_id = property("nyc.event_id")
  }

  object Boston {
    val dayone_event_id = property("boston.dayone_event_id")
    val daytwo_event_id = property("boston.daytwo_event_id")
    val daythree_event_id = property("boston.daythree_event_id")
  }

  object Philly {
    // todo: fill me in when there is a Meetup available
    val eventId = property("boston.dayone_event_id")
  }

  val client: Client = APIKeyClient(property("api_key"))

  def http = Http

  def has_rsvp(eventId: String, tok: oauth.Token) = {
    val mu = OAuthClient(consumer, tok)
    val (res, _) = http(mu.handle(Events.id(eventId)))
    res.flatMap(Event.myrsvp).contains("yes")
  }

  case class SimpleMember(id: String, name: String, photo: String, twttr: Option[String])

  def members(ids: Traversable[String]) = {
    val (res, _) = http(client.handle(Members.member_id(ids.mkString(","))))
    val all =
      for {
        r <- res
        id <- Member.id(r)
        name <- Member.name(r)
        photo <- Member.photo_url(r)
      } yield {
        id -> SimpleMember(id, name, if(photo.isEmpty) DefaultImage else photo, None)
      }
    val twttrs =
      for {
        r <- res
        id <- Member.id(r)
        JString(twttr) <- r \ "other_services" \ "twitter" \ "identifier"
      } yield {
        (id, twttr)
      }
    val mems = (Map(all:_*) /: twttrs)((a,e) =>
      e match {
        case (id, twttr) =>
          if(a isDefinedAt id) a.updated(id, a(id).copy(twttr = Some(twttr)))
          else a
      }
    )
    mems.values
  }

  def member_id(tok: oauth.Token) = {
    val mu = OAuthClient(consumer, tok)
    val (res, _) = http(mu.handle(Members.self))
    res.flatMap(Member.id).apply(0).toInt
  }

  def photos(eventId: String) = {
    val (res, _) = http(client.handle(Photos.event_id(eventId)))
    val result =
      for {
        r <- res
        id <- Photo.photo_id(r)
        hr_link <- Photo.highres_link(r)
        photo_link <- Photo.photo_link(r)
        thumb_link <- Photo.thumb_link(r)
      } yield (id, hr_link, photo_link, thumb_link)
      result map {
        case (id, hires_link, photo_link, thumb_link) =>
          ("id" -> id) ~ ("hires_link" -> hires_link) ~
            ("photo_link" -> photo_link) ~ ("thumb_link" -> thumb_link)
      }
    }

  def rsvps(eventId: String) = {
    def parse(res: List[JValue], meta: List[JValue]): List[JValue] = {
      val result = for {
        r <- res
        id <- Rsvp.id(r)
        name <- Rsvp.name(r)
        photo <- Rsvp.photo_url(r)
        response <- Rsvp.response(r)
        if(response == "yes")
      } yield {
        (id, name, if(photo.isEmpty) DefaultImage else photo)
      }
      val JString(next) = meta \ "next"
      val json = result map {
          case (id, name, photo) =>
            ("id" -> id) ~ ("name" -> name) ~ ("photo" -> photo)
      }
      if(next.isEmpty) json
      else {
        val (r2, m2) = http(url(next) ># (Response.results ~ Response.meta))
        parse(r2, m2) :: json
      }
    }
    val (res, meta) = http(client.handle(Rsvps.event_id(eventId)))
    parse(res, meta)
  }

  def hosting(memberId: String, eventId: String) =
    hosts(eventId).contains(memberId.toInt)

  def hosts(eventId: String) =  {
    val (res, _) = http(client.handle(Events.id(eventId)))
    for {
      e <- res
      JArray(hosts) <- e \ "event_hosts"
      h <- hosts
      JInt(id) <- h \ "member_id"
    } yield id
  }

  def event(eventId: String) = {
      val (res, _) = http(client.handle(Events.id(eventId)))
      val result =
        for {
          e <- res
          cutoff <- Event.rsvp_cutoff(e)
          yes <- Event.rsvpcount(e)
          no <- Event.no_rsvpcount(e)
          limit <- Event.rsvp_limit(e)
        } yield {
          (cutoff, yes, no, limit)
        }
      result map {
        case (cutoff, yes, no, limit) =>
          ("cutoff" ->  cutoff) ~ ("yes" -> yes) ~ ("no" -> no) ~
            ("limit" -> limit)
      }
    }
}
