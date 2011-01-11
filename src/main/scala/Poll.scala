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
  val VOTES = 10
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
        <div id="countdown"><span id="remaining">...</span> votes</div> ++ {
        Random.shuffle(entries.zipWithIndex).map { case (entry, index) =>
          <div class="entry">
             <h4>{entry.speaker}</h4>
             <h4><a href="#" class="vote" id={"v" + index}>Vote</a> {entry.title}</h4>
             <p>{entry.description}</p>
          </div>
        }
      })
    case GET(Params(NoRsvp(params))) => html(
      <p>You must <a href="http://www.meetup.com/ny-scala/calendar/15526582">rsvp</a> to vote!</p>
    )
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
           Scala during the past three years.""") ::
    Entry("Christopher League", "Continuations and Other Functional Patterns",
          """This will be a quick recap and continuation (ha!) of my
          last talk to the NY Scala Enthusiasts, called
          Monadologie. We'll explore a few elegant, useful, and fun
          design patterns from functional programming, with plenty of
          sample code in Scala.""") ::
    Entry("Nathan Hamblen", "Building an HTTP streaming API with Scala",
          """Architecture and design considerations of a live rsvp
          activity stream for the Meetup API that uses RabbitMQ and a
          Netty-Unfiltered server to broadcast messages to many open
          connections using few threads.""") ::
    Entry("Nermin Serifovic", "Scala Performance Considerations",
          """It's a well known fact that Scala performance is
          generally on par with Java. However, using certain language
          constructs can make Scala programs run slow. The goal of
          this talk is to consider trade-offs between code elegance
          and performance.""") ::
    Entry("Alexandre Bertails", "Scala and the Semantic Web",
          """The FeDeRate project from W3C is a sandbox for the
          RDB2RDF Working Group, aiming at exposing Relational
          DataBases to the Semantic Web. In this talk, I propose to
          demo how one can use Scala to interact with the Semantic Web
          using FeDeRate. We will also discuss about the
          implementation.""") ::
    Entry("Runar Bjarnason", "A Quick Tour of Scalaz",
          """Scalaz is an open-source library for Scala which provides
          abstractions to facilitate pure functional programming. A
          brief introduction to Scalaz's pure functional data types
          and type classes, including Monoids, Monads, and all the
          rest.""") ::
    Entry("Runar Bjarnason", "The Guerrilla Guide to Pure Functional Programming",
          """Everything is a function. Every data structure is really
          a program. Every program is a single referentially
          transparent expression. These and other claims are
          substantiated. Leave your side-effects at the door (or at
          least put them in a continuation).""") ::
    Entry("Eugene Yokota", "XML databinding with scalaxb",
          """This talk will be a quick tour of scalaxb, an XML
          databinding tool for Scala that turns XML Schema definition
          into case classes, its use of typeclasses, and restful API
          using Unfiltered.""") ::
    Entry("Viktor Klang", "Building loosely coupled and scalable systems using EDA",
          """Event-driven Architecture (EDA) is a software
          architecture pattern promoting the production, detection,
          consumption of, and reaction to, events. This architectural
          pattern may be applied by the design and implementation of
          applications and systems which transmit events among loosely
          coupled software components and services.""") ::
    Entry("Max Afonov", "Persistence of Scala case classes into MongoDB",
          """MongoDB and Scala are a pair of technologies capable of
          delivering impressive results when used properly
          together. Alas, currently available tools don't provide a
          convenient, simple, fast, and maintainable way of persisting
          Scala objects into MongoDB in a type-safe way. I've spent
          the last 18 months exploring various approaches to this
          problem, and in this talk will explain what dead ends I've
          encountered along the way. To top it off, I'll present my
          newest work in this field, Salat.""") ::
    Entry("Mark Harrah", "sbt 0.9: Why, what, how?",
          """An overview of the changes coming in sbt 0.9, as well as
          the motivations and thinking behind them.""") ::
    Entry("Dean Wampler", "How Scala Influenced Haskell",
          """(or is it the other way around?... ;^) Scala borrows many
          good ideas from Haskell, from syntactic elements to major
          features. I'll discuss some of these influences and also
          describe ways in which Scala and Haskell are different.""") ::
    Entry("Dean Wampler", "The Design of the Scala 2.8 Collections",
          """The collections library was significantly revised in
          Scala 2.8. Some of the changes solved design problems, such
          the desire to return new instances of the same input type,
          rather than a common parent type. Other changes make it
          easier for user's to create their own collections that
          exploit reusable features. I'll describe several of these
          changes, using examples.""") ::
    Entry("Maxime Lévesque", "Presentation of Squeryl",
          """Squeryl aims to be an ORM/DSL for talking with Databases
          with minimum verbosity and maximum type safety. The
          presentation will be a quick tour of the features, I will
          then go over the design decisions and compromises that were
          made in attempting to blend the Relationnal, FP, and OO
          paradigms together, followed by a discussion on how implicit
          conversions and the type system were used to enforce the
          typing rules prescribed by SQL, via a type arythmetic.""") :: Nil
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

object NoRsvp extends Params.Extract("norsvp", Params.first)
