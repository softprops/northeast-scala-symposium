package nescala

import dispatch._ // for future enrichment
import unfiltered.Cycle.Intent
import unfiltered.request._
import unfiltered.request.QParams._
import unfiltered.response._
import unfiltered.Cookie
import com.ning.http.client.oauth.RequestToken
import scala.concurrent.ExecutionContext.Implicits.global
import nescala.request.UrlEncoded

object NESS extends Config {

  def site: Intent[Any, Any] = {    
    case GET(Path(Seg("login" :: Nil))) & Params(p) =>
      val callbackbase = s"${property("host")}/authenticated"
      val callback = p("then") match {
        case Seq(after) => s"$callbackbase?then=$after"
        case _ => callbackbase
      }
      Meetup.AuthExchange.fetchRequestToken(callback).apply().fold({
        ResponseString(_)
      }, { t =>
        // drop cookie and redirect to meetup for auth
        val to = Meetup.AuthExchange.signedAuthorize(t)
        SetCookies(
          Cookie("token",
                 ClientToken(
                   t.getKey,
                   t.getSecret,
                   Hashing(t.getKey, t.getSecret)).toCookieString,
                 httpOnly = true)) ~> Redirect(to)
      })

    case GET(Path(Seg("logout" :: Nil))) =>
      SetCookies.discarding("token") ~> Redirect("/")

    case req @ GET(Path(Seg("authenticated" :: Nil))) & Params(params) =>
      val expected = for {
        verifier <- lookup("oauth_verifier") is
          required("verifier is required") is
          nonempty("verifier can not be blank")
        token <- lookup("oauth_token") is
          required("token is required") is
          nonempty("token can not be blank")
        after <- lookup("then") is optional[String, String]
      } yield {
        CookieToken(req).map { rt =>
          Meetup.AuthExchange.fetchAccessToken(rt.token, verifier.get).apply().fold({
            ResponseString(_)
          }, { at =>
            val mid = Some(Meetup.member_id(at).toString)
            SetCookies(
              Cookie("token",
                     ClientToken(
                       at.getKey,
                       at.getSecret,
                       Hashing(
                         at.getKey, at.getSecret, verifier.get, mid.get),
                       verifier,
                       mid).toCookieString,
                     httpOnly = true)) ~> Redirect(after.get match {
                case Some("vote") => "/2014/talks#proposed"
                  case Some("talk") => "/#propose-talk"
                  case Some("proposals") => "/2014/talk_tally"
                  case Some("panel_proposals") => "/2014/panel_tally"
                  case other => "/"
                })
          })
        }.getOrElse(sys.error("could not find request token"))
      }

      expected(params) orFail { errors =>
        BadRequest ~> ResponseString(errors.map { _.error } mkString(". "))
      }
  }

  def http = dispatch.Http
}
