package com.meetup

import models.Vote
import scala.util.Random
import unfiltered.request._
import unfiltered.response._
import dispatch.oauth.Token
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import scala.collection.JavaConversions._

object Poll {
  val VOTES = 5
  def intent: unfiltered.filter.Plan.Intent = {
    case POST(Params(params) & CookieToken(ClientToken(v, s, Some(c)))) =>
      Storage { mgr =>
        val member_id = Meetup.member_id(Token(v,s))
        val query = mgr.newQuery(classOf[Vote], "member_id == id_param")
        query.declareParameters("int id_param")
        val votes = query.execute(member_id).asInstanceOf[java.util.List[Vote]]
        val ids = votes map { _.entry_id }

        val opt_id = params("entry_id").headOption.map { 
          _.toInt 
        }.filterNot(ids.contains).map { entry_id =>
          val vote = new Vote
          vote.entry_id = entry_id.toInt
          vote.member_id = member_id
          mgr.makePersistent(vote)
          entry_id
        }
        JsonContent ~> ResponseString(compact(render(ids ++ opt_id)))
      }
    case GET(CookieToken(ClientToken(v, s, Some(c)))) =>
      html(
        <p>Votes remaining: <span id="remaining">...</span></p> ++ {
        Random.shuffle(entries.zipWithIndex).map { case (entry, index) =>
          <div class="entry">
             <h4>{entry.speaker}</h4>
             <h4><a href="#" class="vote" id={"v" + index}>Vote</a> {entry.title}</h4>
             <p>{entry.description}</p>
          </div>
        }
      })
    case GET(_) => html(<p><a href="/connect">Sign in with Meetup</a></p>)
    case _ => BadRequest
  }
  def html(body: xml.NodeSeq) = Html(
    <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <title>&#8663;northeast scala symposium</title>
        <link href="http://fonts.googleapis.com/css?family=Arvo:regular,bold" rel="stylesheet" type="text/css"/>
        <link rel="stylesheet" type="text/css" href="css/tipsy.css" />
        <link rel="stylesheet" type="text/css" href="css/app.css" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.min.js"></script>
        <script type="text/javascript" src="/js/vote.js"></script>
      </head>
      <body>
        <div id="container">
        <h1>Symposium Talk Selection</h1>
        { body }
        </div>
      </body>
    </html>
  )
  val entries = 
    Entry("Jorge Ortiz", "Panel: Scaling with Scala",
          """There are many people running Scala code in production
          environments with thousands or millions of users, and many
          people writing Scala code in teams of dozens or more. This
          panel will bring together people with experience writing
          software for many users or with large teams, and discuss the
          joys and pitfall of scaling Scala in every sense.""") ::
    Entry("Jorge Ortiz", "Foursquare's Query Language",
          """Foursquare has written a type-safe query language for
          MongoDB that extends Lift's Record. It is extremely
          expressive (almost any MongoDB query can be expressed) and
          yet extremely type-safe (parameters to queries, operations
          on fields, and results are all statically typed). This talk
          will explore the API and the implementation of Foursquare's
          Query Language.""") :: Nil
}
case class Entry(speaker: String, title: String, description: String)

object Storage {
  def apply[T](block: javax.jdo.PersistenceManager => T): T = {
    val mgr = factory.getPersistenceManager
    try { block(mgr) }
    finally { mgr.close() }
  }
    
  lazy val factory =
    javax.jdo.JDOHelper.getPersistenceManagerFactory("transactions-optional")
}
