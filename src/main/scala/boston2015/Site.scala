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

  def talks(anchor: String = "") =
    if (anchor.nonEmpty) Redirect(s"/2015/talks#$anchor")
    else Redirect("/2015/talks")

  def proposeit
   (session: SessionCookie,
    params: Params.Map,
    id: Option[String] = None): ResponseFunction[Any] =
    if (!session.nescalaMember) talks() else {
      val cached = Member.get(session.member.toString)
      val expected = for {
        name <- lookup("name") is required("name is required")
        desc <- lookup("desc") is required("desc is required")
        kind <- lookup("kind") is required("kind is required")
      } yield id match {
        case None =>
          Proposal.create(cached.get, name.get, desc.get, kind.get)
          .fold({ err =>
             println(s"create err $err")
             talks("propose")
           }, {
             case (_, created) =>
               talks(created.domId)
           })
        case Some(key @ Proposal.Pattern(memberid, _))
          if memberid == session.member.toString =>
          Proposal.edit(cached.get, key, name.get, desc.get, kind.get)
           .fold({ err =>
              println(s"edit err $err")
              talks("propose")
            }, { updated =>
                talks(updated.domId)
            })
        case _ =>
          // key provided but its not ours
          talks()
      }
      expected(params).orFail { errors =>
        talks("propose")
      }
    }

  def pages: Intent[Any, Any] = {
    case GET(req) & Path(Seg(Nil)) =>
      respond(req)(indexPage)
    case GET(req) & Path(Seg("2015" :: "talks" :: Nil)) =>
      respond(req)(proposalsPage(Proposal.all))
    case POST(req) & Path(Seg("2015" :: "talks" :: Nil)) & Params(params) =>
      respond(req) {
        case Some(member) =>
          proposeit(member, params)
        case _ =>
          talks()
      }
    case POST(req) & Path(Seg("2015" :: "talks" :: UrlDecoded(id) :: Nil)) & Params(params) =>
      respond(req) {
        case Some(member) =>
          proposeit(member, params, Some(id))
        case _ =>
          talks()
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
