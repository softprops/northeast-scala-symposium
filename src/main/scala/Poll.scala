package com.meetup

import scala.util.Random
import unfiltered.response._
import dispatch.oauth.Token

object Poll {
  val VOTES = 5
  def intent: unfiltered.filter.Plan.Intent = {
    case CookieToken(ClientToken(v, s, Some(c))) =>
      html(
        <p>You have 5 votes remaining</p> ++ {
        Random.shuffle(entries.zipWithIndex).map { case (entry, index) =>
          <div>
             <h4>{entry.speaker}</h4>
             <h4><a href="#" id={"talk_" + index}>Vote: </a>{entry.title}</h4>
             <p>{entry.description}</p>
          </div>
        }
      })
    case _ => html(<p><a href="/connect">Sign in with Meetup</a></p>)
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

