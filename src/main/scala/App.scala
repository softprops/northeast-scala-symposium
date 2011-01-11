package com.meetup

import unfiltered.request._
import unfiltered.response._

import unfiltered.Cookie
import dispatch.meetup.Auth
import dispatch.oauth.{Consumer,Token}

/** Unfiltered plan */
class App extends unfiltered.filter.Plan with Config {
  import QParams._

  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._

  def intent = {
    case GET(Path("/rsvps") & Jsonp.Optional(jsonp)) =>
      JsonContent ~> ResponseString(jsonp.wrap(compact(render(Meetup.rsvps))))

    case GET(Path("/event") & Jsonp.Optional(jsonp)) =>
      JsonContent ~> ResponseString(jsonp.wrap(compact(render(Meetup.event))))

    case req @ GET(Path("/vote")) => Poll.intent(req)

    case GET(Path("/connect")) =>
      val callback = "%s/authenticated" format(property("host"))
      val t = http(Auth.request_token(consumer, callback))
      ResponseCookies(
        Cookie("token", ClientToken(t.value, t.secret, None).toCookieString)) ~>
          Redirect(Auth.authenticate_url(t).to_uri.toString)

    case request @ GET(Path("/disconnect")) =>
      ResponseCookies(Cookie("token", "")) ~> Redirect("/vote")

    case request @ GET(Path("/authenticated") & Params(params)) =>
      val expected = for {
        verifier <- lookup("oauth_verifier") is
          required("verifier is required") is
          nonempty("verifier can not be blank")
        token <- lookup("oauth_token") is
          required("token is required") is
          nonempty("token can not be blank")
      } yield {
        CookieToken(request) match {
          case Some(rt) =>
            val at = http(Auth.access_token(consumer, Token(rt.value, rt.sec), verifier.get))
            ResponseCookies(
               Cookie("token",
                      ClientToken(at.value, at.secret, verifier)
                      .toCookieString)) ~> Redirect("/")
          case _ => error("could not find request token")
        }
      }

      expected(params) orFail { errors =>
        BadRequest ~> ResponseString(errors.map { _.error } mkString(". "))
      }

   //case GET(Path("/twttr", Jsonp.Optional(jsonp, _))) =>
   //   JsonContent ~> ResponseString(jsonp.wrap(compact(render(Twitter.tweets))))
  }
  implicit def http = new dispatch.AppEngineHttp
  lazy val consumer = Consumer(
    property("mu_consumer"), property("mu_consumer_secret"))
}
