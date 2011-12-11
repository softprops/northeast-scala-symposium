package nescala

object NESS extends Config {
  import unfiltered.request._
  import unfiltered.response._
  import unfiltered.Cookie
  import QParams._
  import dispatch.meetup._
  import dispatch.oauth.Token
  import nescala.Meetup.Cities

  def site: unfiltered.Cycle.Intent[Any, Any] = {
    //case GET(Path("/")) => Redirect("/2011")
    
    case GET(Path("/connect")) & Params(p) =>
      val callback = "%s/authenticated" format property("host")
      val t = http(Auth.request_token(Meetup.consumer, callback))
      ResponseCookies(
        Cookie("token", ClientToken(t.value, t.secret, None, None).toCookieString)) ~>
          Redirect(Auth.authenticate_url(t).to_uri.toString)

    case GET(Path("/disconnect")) =>
      ResponseCookies(Cookie("token", "", maxAge = Some(0))) ~> Redirect("/")

    case req @GET(Path("/authenticated") & Params(params)) =>
      val expected = for {
        verifier <- lookup("oauth_verifier") is
          required("verifier is required") is
          nonempty("verifier can not be blank")
        token <- lookup("oauth_token") is
          required("token is required") is
          nonempty("token can not be blank")
        then <- lookup("then") is optional[String, String]
      } yield {
        CookieToken(req) match {
          case Some(rt) =>
            val at = http(Auth.access_token(Meetup.consumer, Token(rt.value, rt.sec), verifier.get))
            ResponseCookies(
                Cookie("token",
                       ClientToken(at.value, at.secret, verifier, Some(Meetup.member_id(at).toString))
                       .toCookieString)) ~> Redirect("/#propose-talk")
          case _ => sys.error("could not find request token")
        }
      }

      expected(params) orFail { errors =>
        BadRequest ~> ResponseString(errors.map { _.error } mkString(". "))
      }
  }

  implicit def http = new dispatch.Http
}
