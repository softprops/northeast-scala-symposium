package nescala

import dispatch._ // for future enrichment
import unfiltered.Cycle.Intent
import unfiltered.request.{ GET, Params, Path, Seg, & }
import unfiltered.response.{ Redirect, SetCookies }
import unfiltered.Cookie
import com.ning.http.client.oauth.RequestToken
import scala.concurrent.ExecutionContext.Implicits.global
import nescala.request.UrlEncoded

object Northeast extends Config {

  object Error extends Params.Extract("error", Params.first)
  object State extends Params.Extract("state", Params.first)
  object Code extends Params.Extract("code", Params.first)

  val callback = s"${property("host")}/authenticated"

  def index = Redirect("/")

  def site: Intent[Any, Any] = {    

    case GET(Path(Seg("login" :: Nil))) & Params(p) =>
      Redirect(Meetup.authorize(callback, State.unapply(p)))

    case GET(Path(Seg("logout" :: Nil))) =>
      SessionCookie.discard ~> Redirect("/")

    case req @ GET(Path(Seg("authenticated" :: Nil))) & Params(params) =>
      params match {
        case Error(error) =>
          index
        case Code(code) =>
          Meetup.access(code, callback).map {
            case (access, refresh) =>
              val session = Session.create(access, refresh)
              val member = nescala.boston2015.Member
                .sync(session.memberId.apply().toString)
              SessionCookie.drop(session) ~>
                Redirect(State.unapply(params) match {
                  case Some("propose") =>
                    "/2015/talks#speak"
                  case Some(value) =>
                    s"/2015/talks#$value"
                  case _ =>
                    "/"
                })
            case _ =>
              index
          }.getOrElse {
            index
          }
        case _ =>
          index
      }
  }

  def http = dispatch.Http
}
