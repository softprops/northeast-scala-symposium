package nescala.boston2015

import dispatch._ // for future pimping
import dispatch.Defaults._
import nescala.{ Meetup, SessionCookie }
import nescala.request.UrlDecoded
import unfiltered.request.{ GET, HttpRequest, Params, Path, POST, Seg, & }
import unfiltered.request.QParams._
import unfiltered.response.{ Redirect, ResponseString, ResponseFunction, Unauthorized }
import unfiltered.Cycle.Intent
import scala.util.control.NonFatal

object Site extends Templates {

  def talks = Redirect("/2015/talks")

  def pages: Intent[Any, Any] = {
    case GET(req) & Path(Seg(Nil)) =>
      respond(req)(indexPage)
    case GET(req) & Path(Seg("2015" :: "talks" :: Nil)) =>
      respond(req)(proposalPage)
    case POST(req) & Path(Seg("2015" :: "talks" :: Nil)) & Params(params) =>
      respond(req) {
        case session @ Some(member) =>
          if (!member.nescalaMember) proposalPage(session) else {
            val cached = Member.get(member.member.toString)
            val expected = for {
              name <- lookup("name") is required("name is required")
              desc <- lookup("desc") is required("desc is required")
              kind <- lookup("kind") is required("kind is required")
            } yield Proposal.create(
              cached.get, name.get, desc.get, kind.get).fold({
              err =>
                println(s"err $err")
                proposalPage(session)
            }, {
              case _ =>
                talks
            })
            expected(params).orFail { errors =>
              println(s"errors $errors")
              proposalPage(session)
            }
          }
        case _ =>
          talks
      }
    case POST(req) & Path(Seg("2015" :: "talks" :: UrlDecoded(id) :: Nil)) & Params(params) =>
      respond(req) {
        case session @ Some(member) =>
          // todo: fill me in
          proposalPage(session)
        case _ =>
          talks
      }
  }

  def respond(req: HttpRequest[_])(handle: Option[SessionCookie] => ResponseFunction[Any]) =
    req match {
      case SessionCookie.Value(sc) =>
        sc.fold(handle(None), { member => handle(Some(member)) })
      case _ =>
        handle(None)
    }
}
