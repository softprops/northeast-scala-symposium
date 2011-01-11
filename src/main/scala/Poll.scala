package com.meetup

import models.Vote
import scala.util.Random
import unfiltered.request._
import unfiltered.response._
import dispatch.oauth.Token
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import scala.collection.JavaConversions._
import java.lang.{Integer => JInt}

object Poll {
  val VOTES = 5
  def intent: unfiltered.filter.Plan.Intent = {
    case POST(Params(params) & CookieToken(ClientToken(v, s, Some(c)))) =>
      Storage { mgr =>
        val member_id = Meetup.member_id(Token(v,s))
        val query = mgr.newQuery(classOf[Vote], "member_id == id_param")
        query.declareParameters("int id_param")
        def q_ids = {
          val votes = query.execute(member_id).asInstanceOf[java.util.List[Vote]]
          votes map { _.entry_id }
        }
        val ids = q_ids
        params("entry_id").headOption.map { _.toInt }.foreach { entry_id =>
          params("action") match {
            case Seq("Vote") if ids.size < VOTES && !ids.contains(entry_id) =>
              val vote = new Vote
              vote.entry_id = entry_id.toInt
              vote.member_id = member_id
              mgr.makePersistent(vote)
            case Seq("Undo") =>
              val del = mgr.newQuery(classOf[Vote])
              del.setFilter("member_id == member_param")
              del.setFilter("entry_id == entry_param")
              del.declareParameters("int member_param, int entry_param")
              del.deletePersistentAll(new JInt(member_id), new JInt(entry_id))
            case _ => ()
          }
        }
        JsonContent ~> ResponseString(compact(render(q_ids)))
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
          Query Language.""") ::
    Entry("Harry Heymann", "Advanced Lift Techniques",
          """Currently the largest lift site in production (I think?),
          foursquare serves over 1000 requests per second to users of
          our website and Rest API. Over the past year and a half
          we've developed some interesting techniques for building
          large scale Lift based applications. We've also worked with
          dpp and the Lift team to continue to push the framework as a
          whole. I will show off some of these techniques in
          foursquare code and answer questions about running Lift in
          production.""") ::
    Entry("Brendan W. McAdams", "MongoDB + Scala",
          """I'd love to do an updated version of my talk on MongoDB +
          Scala. Casbah 2.0 will be out and well along by the time
          this runs. It'll also give me a chance to wax on Pimp My
          Library, implicit conversions, type aliasing and all the
          other fun things aspiring coders should know.""") ::
    Entry("Josh Suereth", 
          "Implicits without import tax: How to make clean APIs with implicits",
          """This talk covers how to utilize Scala implicits for
          powerful, expressive APIs without requiring explicit import
          statements for the user. These techiniques will help you
          improve your own libraries .... """) ::
    Entry("Paul Chiusano", "Actors: can we do better?",
          """Actors have gotten a lot of attention as an approach to
          writing concurrent programs. Unfortunately, actors rely on
          side effects and as a result have limited composability. In
          this talk I'll explore an alternative to actors that can be
          used to express stateful concurrent computations without
          resorting to side effects and without destroying
          composability.""") ::
    Entry("Paul Chiusano", 
          "Experience report: Scala and purely functional programming at Capital IQ",
           """Capital IQ has been a commercial user of Scala since
           2008 and we've primarily used Scala as a purely functional
           language. During that time we've gone from one person using
           Scala for a single project to about ten people using or
           having used Scala on several projects. I'll talk about the
           good, the bad, and the ugly of our experiences with using
           Scala during the past three years.""") :: Nil
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
