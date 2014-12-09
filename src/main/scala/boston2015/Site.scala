package nescala.boston2015

import dispatch._ // for future pimping
import dispatch.Defaults._
import nescala.{ Meetup, SessionCookie }
import nescala.request.UrlDecoded
import org.joda.time.{ DateMidnight, DateTimeZone }
import unfiltered.request.{ DELETE, GET, HttpRequest, Params, Path, POST, Seg, & }
import unfiltered.request.QParams._
import unfiltered.response.{ JsonContent, Redirect, ResponseString, ResponseFunction, Unauthorized }
import unfiltered.Cycle.Intent
import scala.util.control.NonFatal
import scala.util.Random

object Site extends Templates {

  val DayOneEvent = 218741329
  val TZ = DateTimeZone.forID("US/Eastern")
  val proposalCutoff = // tuesday @ mignight
    new DateMidnight(TZ).withYear(2014).withMonthOfYear(12).withDayOfMonth(9)

  def proposalsOpen = proposalCutoff.isAfterNow
  def votesOpen = !proposalsOpen // todo: set a cutoff for this

  def talks(anchor: String = "") =
    if (anchor.nonEmpty) Redirect(s"/2015/talks#$anchor")
    else Redirect("/2015/talks")

  def tally(props: Iterable[Proposal]) = {
    val sb = new StringBuilder()
    val grouped = props.groupBy(_.kind)
    sb.append(s"${props.size} proposals\n")
    Array("medium", "short", "lightning").foreach { len =>
      sb.append(s"\n# $len proposals\n")
      grouped(len).toSeq.sortBy { p => (-p.votes, p.name) }.foreach { p =>
        sb.append(p.votes).append(" ").append(p.name).append(" (").append(p.member.map(_.name).getOrElse("?")).append(")\n")
      }
    }
    ResponseString(sb.toString)
  }

  def vote
   (session: SessionCookie,
    id: String,
    yes: Boolean): ResponseFunction[Any] =
    if (!session.canVote) talks() else {
      def err(msg: String) =
        s"""{"status":400, "msg":"$msg"}"""
      def ok(remaining: Int) =
        s"""{"status":200, "remaining":$remaining}"""
      JsonContent ~> ResponseString(
        Proposal.vote(session.member, id, yes)
          .fold(err, ok))
    }

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
      respond(req)(proposalsPage(Random.shuffle(Proposal.all)))
    case POST(req) & Path(Seg("2015" :: "talks" :: Nil)) & Params(params) =>
      respond(req) {
        case Some(member) =>
          proposeit(member, params)
        case _ =>
          talks()
      }
    case GET(req) & Path(Seg("2015" :: "talks" :: "peek" :: Nil)) =>
      respond(req) {
        case Some(member) if Meetup.hosts(member.session, DayOneEvent).apply().exists(_ == member.member) =>
          tally(Proposal.all)
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
    case req @ Path(Seg("2015" :: "talks" :: UrlDecoded(id) :: "votes" :: Nil)) & Params(params) =>        
      respond(req) {
        case Some(member) =>
          req match {
            case POST(_) =>
              vote(member, id, true)
            case DELETE(_) =>
              vote(member, id, false)
            case _ =>
              talks()
          }
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
