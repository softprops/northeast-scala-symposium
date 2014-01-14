package nescala.nyc2014

import nescala.Meetup

trait Templates {

  val meetupGroup = "http://www.meetup.com/nescala/"
  val dayoneLink = s"${meetupGroup}events/${Meetup.Nyc2014.dayoneEventId}/"
  val daytwoLink = s"${meetupGroup}events/${Meetup.Nyc2014.daytwoEventId}/"

  def btn(link: String, display: String) = 
    <a href={link} class="button">{display}</a>

  def layout(head: xml.NodeSeq)
    (bodyScripts: xml.NodeSeq)
    (body: xml.NodeSeq) = unfiltered.response.Html5(
      <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <meta name="viewport" content="width=device-width,initial-scale=1"/>
        <title>&#8663;northeast scala symposium</title>
        <link href="http://fonts.googleapis.com/css?family=Source+Code+Pro|Montserrat:700|Open+Sans:300italic,400italic,700italic,400,300,700" rel="stylesheet" type="text/css"/>
        <link rel="stylesheet" type="text/css" href="/css/gridism.css" />
        <link rel="stylesheet" type="text/css" href="/css/normalize.css" />
        <link rel="stylesheet" type="text/css" href="/css/nyc2014.css" />
        <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
        { head }
      </head>
      <body class="wrap wider" id="top">
        { body }
        <footer>
          <div class="grid">
            <div class="unit whole">
              <a href="#top">nescala</a> is made possible with <span class="love">&#10084;</span> from the
              <div>
                <a href="http://www.meetup.com/boston-scala/">Boston</a>,
                <a href="http://www.meetup.com/scala-phase/">Philadelphia</a>,
                and <a href="http://www.meetup.com/ny-scala/">New York</a> scala enthusiasts and, of course, of all of
                <a href="http://www.meetup.com/nescala/photos/">you</a>.
              </div>
            </div>
            <div class="unit whole">
              { twttrFollow }
            </div>
            <div class="unit whole">
              hosting by <a href="http://www.heroku.com/">heroku</a>
            </div>
            <div class="center">
              <div class="unit whole">
                <p>
                  This year's symposium, uses structual sharing with those, 3 years passed.
                </p>
              </div>
              <p class="unit one-third">
                <a href="/2013">2013</a>
              </p>
              <p class="unit one-third">
                <a href="/2012">2012</a>
              </p>
              <p class="unit one-third">
                <a href="/2011">2011</a>
              </p>
            </div>
          </div>
        </footer>
        <script type="text/javascript" src="/js/nyc2014/nyc.js"></script>
        { bodyScripts }
      </body>
    </html>
  )
  
  val twttrFollow = {
    <a href="https://twitter.com/nescalas" class="twitter-follow-button" data-show-count="false" data-size="large">Follow @nescalas</a>
  }

  def login(authed: Boolean, after: String = "") = <span></span>
   /* if (!authed) <div id="auth-bar" class="unit whole">
      <div>
        <div class="l">Just who are you anyway?</div>
        <div class="r">
          <a href={ "/login%s" format(if (after.isEmpty) "" else s"?then=$after") } class="btn login">Log in with Meetup</a>
        </div>
      </div>
    </div> else <span></span> */

  def head(authed: Boolean, afterlogin: String = "") =
   <header>
    <div class="grid">
      <div class="unit whole center title">
        <div class="circle">
          <a href="/">
            <h1>
              north°<br/>
              scala
            </h1>
            <h2 class="amp mute">symposium</h2>
          </a>
        </div>
        <div class="center">
          <hr/>
          <h4><a href="#where">New York, NY</a></h4>
          <h4>March 1 <span class="amp">&amp;</span> 2, 2014</h4>
        </div>
        </div>{ login(authed, afterlogin) }
      </div>
    </header>
  
  def blurb(authed: Boolean) = (<section>
    <div class="grid">
      <div class="unit whole lead center">
        <p>
          A gathering of statically refined types
        </p>
        <p class="amp">&amp;</p>
        <p>
          well-spoken peers
        </p>
        <hr/>
        <p class="small mute">
         self-selected by you, from the northeasterly regions of the US
        </p>
      </div></div></section><section><div class="grid">
      <div class="unit half">
        <h2>One day of <strong>sharing</strong>.</h2>
        <p>
         Day 1 is back to basics with one room, one track of talks. 
        </p>
        <p>
          Seating on day 1 is limited. The first block of RSVPs will open Wednesday January 15 at noon. We've set a cap of 50 for this block and expect these to sell out quickly to punctual, signed in members of this meetup who have paypal accounts and know how to use them.
        </p>
        <p>
          After this block does sell out, we'll announce a date and time for the second and final block.
        </p>        
      </div>
      <div class="unit half">
        <h2>One day of <strong>pairing</strong>.</h2>
        <p>
         Day 2 will be an informal unconference self-organized on the spot by whoever shows up.
        </p>
        <p>
          More info to follow. 
        </p>
      </div>
      <div class="unit whole" id="where">
        <hr/>
        <h2>Come. Find us.</h2>
        <p>
          This year's symposium will be held @ <a target="_blank" href="http://meetup.com/">Meetup</a> <a href="http://www.gramfeed.com/instagram/tags#meetuphq">HQ</a>.
        </p>
        <iframe src="https://maps.google.com/maps?f=q&amp;hl=en&amp;q=632+Broadway,+New+York,+NY,+10012,+us&amp;ie=UTF8&amp;hq=&amp;hnear=632+Broadway,+New+York,+10012&amp;ll=40.726166,-73.996023&amp;spn=0.002529,0.003578&amp;t=m&amp;z=14&amp;output=embed"></iframe>
      </div>
    </div>
  </section>)

  def indexPage(authed: Boolean) =
    layout(Nil)(
    <script type="text/javascript" src="/js/jquery.scrollTo-1.4.2-min.js"></script>
    <script type="text/javascript" src="/js/nyc2014/nyc.js"></script>)(
      head(authed) ++ blurb(authed)
    )
}

object Templates extends Templates {}

