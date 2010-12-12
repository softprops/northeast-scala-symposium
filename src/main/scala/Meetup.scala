package com.meetup

trait Config {
  private lazy val props = {
    val file = getClass.getResourceAsStream("/meetup.properties")
    val props = new java.util.Properties
    props.load(file)
    file.close()
    props
  }

  def property(name: String) = props.getProperty(name) match {
    case null => error("missing property %s" format name)
    case value => value
  }

  def intProperty(name: String) =
    try {
      property(name).toInt
    } catch { case nfe: NumberFormatException => 
      error("%s was not an int" format property(name))
    }
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
  val eventCache = cache("events")

  def rsvps = {
    import net.liftweb.json.JsonParser._
    val json = rsvpCache.getOr("current")({
      val (res, _) = client.call(Rsvps.event_id(event_id))
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
      })), Some(System.currentTimeMillis + intProperty("ttl")))
    })
    parse(json)
  }


  def event = {
    import net.liftweb.json.JsonParser._
    val json = eventCache.getOr(event_id) {
      val (res, _) = client.call(Events.id(event_id))
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
      (compact(render(result map {
        case (cutoff, yes, no, limit) =>
          ("cutoff" ->  cutoff) ~ ("yes" -> yes) ~ ("no" -> no) ~ 
            ("limit" -> limit)
      })), Some(System.currentTimeMillis + intProperty("ttl")))
    }
    parse(json)
  }
}
