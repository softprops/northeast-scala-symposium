package com.meetup

object Meetup extends JsonCached with Config {
  import dispatch._
  import meetup._
  import dispatch.liftjson.Js._
  import oauth._
  import Http._

  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.JsonParser._

  val event_id = property("event_id")
  val client: Client = APIKeyClient(property("api_key"))
  implicit def http = new dispatch.AppEngineHttp

  def rsvps =
    cacheOr("rsvps", "current") {
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
      (result map {
        case (id, name, photo) =>
          ("id" -> id) ~ ("name" -> name) ~ ("photo" -> photo)
      }, Some(System.currentTimeMillis + intProperty("ttl")))
    }

  def event =
    cacheOr("events", event_id) {
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
      (result map {
        case (cutoff, yes, no, limit) =>
          ("cutoff" ->  cutoff) ~ ("yes" -> yes) ~ ("no" -> no) ~
            ("limit" -> limit)
      }, Some(System.currentTimeMillis + intProperty("ttl")))
    }
}
