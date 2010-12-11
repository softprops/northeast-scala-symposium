package com.meetup

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

object Meetup extends Config {
  import dispatch._
  import meetup._
  import dispatch.liftjson.Js._
  import oauth._
  import Http._

  val event_id = property("event_id")
  val client: Client = APIKeyClient(property("api_key"))
  implicit def http = new dispatch.AppEngineHttp
  def rsvps: Seq[(String, String, String)] = {
    val (res, meta) = client.call(Rsvps.event_id(event_id))
    val defaultImage = "http://img1.meetupstatic.com/39194172310009655/img/noPhoto_50.gif"
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
  }
}
