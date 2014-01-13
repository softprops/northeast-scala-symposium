package nescala.nyc2014

import nescala.Meetup
import scala.collection.immutable.TreeMap

trait Templates {
  import java.net.URLEncoder.encode

  val meetupGroup = "http://www.meetup.com/nescala/"
  val eventLink = s"${meetupGroup}events/${Meetup.Nyc2014.dayoneEventId}/"

  def layout(head: xml.NodeSeq)
    (bodyScripts: xml.NodeSeq)
    (body: xml.NodeSeq) = unfiltered.response.Html5(
      <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <title>&#8663;northeast scala symposium</title>
        <link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Arvo:regular,bold"/>
        <link rel="stylesheet" type="text/css" href="/css/nyc2014.css" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
        { head }
      </head>
      <body>
        { body }
        <div id="footer">
          made possible with <span class="love">&#10084;</span> from the <div><a href="http://www.meetup.com/boston-scala/">Boston</a>, <a href="http://www.meetup.com/scala-phase/">Philadelphia</a>, and <a href="http://www.meetup.com/ny-scala/">New York</a> scala enthusiasts,</div>
        <div class="divided">hosting from <a href="http://www.heroku.com/">heroku</a></div>
          <div id="last-year">
            <div>What happen to last year? It ended.</div>
            <a href="/2013"><img src="/images/ne.png"/></a>
          </div>
        </div>
      <script type="text/javascript" src="/js/nyc2014/nyc.js"></script>
      { bodyScripts }
      </body>
    </html>
  )
  
  val twttrFollow = {
    <a href="https://twitter.com/nescalas" class="twitter-follow-button" data-show-count="false" data-size="large">Follow @nescalas</a>
  }

  def login(authed: Boolean, after: String = "") =
    if (!authed) <div id="auth-bar" class="clearfix"><div class="contained"><div class="l">Just who are you anyway?</div><div class="r"><a href={ "/login%s" format(if(after.isEmpty) "" else "?then=%s".format(after)) } class="btn login">Log in with Meetup</a></div></div></div> else <span></span>

  def head(authed: Boolean, afterlogin: String = "") =
   <div id="head" class="clearfix">
    <div class="contained">
      <a id="home-link" href="/" class="clearfix">
        <div>
          <h1>ne<span>sc</span></h1>
          <h1><span>ala</span>s</h1>
          <h1>ympo</h1>
          <h1>sium</h1>
        </div>
        <img src="/images/bell.png" id="bell"/>
      </a>
      <div>
        <h4>Scala community liberation</h4>
        <h4>in <a href="/#where">Philly</a></h4>
        <h4>Feb 8-9, 2013</h4>
        <hr/>
      </div>
    </div>
   </div> ++ { login(authed, afterlogin) }
  
  def blurb(authed: Boolean) =
    <div id="blurb">
      <div class="contained">
      <p>Just <a href={ eventLink }>RSVP</a> <span class="amp">&amp;</span> you're in.</p>
      <hr/>
      { twttrFollow }
      </div>
    </div>

  def indexPage(authed: Boolean) =
    layout(Nil)(
    <script type="text/javascript" src="/js/jquery.scrollTo-1.4.2-min.js"></script>
    <script type="text/javascript" src="/js/nyc2014/index.js"></script>)(
      head(authed) ++ blurb(authed)
    )
}

object Templates extends Templates {}

