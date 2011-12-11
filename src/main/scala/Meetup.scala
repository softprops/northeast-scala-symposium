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

  object Cities {
    val nyc = "nyc"
    val boston = "boston"
  }

  lazy val consumer = Consumer(
    property("mu_consumer"), property("mu_consumer_secret"))

  object Nyc {
    val event_id = property("nyc.event_id")
  }

  object Boston {
    val event_id = property("boston.event_id")
  }

  val client: Client = APIKeyClient(property("api_key"))

  implicit def http = new Http

  def has_rsvp(city: String, tok: oauth.Token) = {
    val mu = OAuthClient(consumer, tok)
    val (res, _) = http(mu.handle(Events.id(city match {
      case Cities.nyc => Nyc.event_id
      case _ => Boston.event_id
    })))
    res.flatMap(Event.myrsvp).contains("yes")
  }

  def member_id(tok: oauth.Token) = {
    val mu = OAuthClient(consumer, tok)
    val (res, _) = http(mu.handle(Members.self))
    res.flatMap(Member.id).apply(0).toInt
  }

  def photos(city: String) = {
    val (res, _) = http(client.handle(Photos.event_id(city match {
      case Cities.nyc => Nyc.event_id
      case _ => Boston.event_id                                                
    })))
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

  def rsvps(city: String) = {
      val (res, _) = http(client.handle(Rsvps.event_id(city match {
        case Cities.nyc => Nyc.event_id
        case _ => Boston.event_id
      })))
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
      result map {
        case (id, name, photo) =>
          ("id" -> id) ~ ("name" -> name) ~ ("photo" -> photo)
      }
    }

  def event(city: String) = {
      val (res, _) = http(client.handle(Events.id(city match {
        case Cities.nyc => Nyc.event_id
        case _  => Boston.event_id
      })))
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
