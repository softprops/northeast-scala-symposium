package nescala.boston2015

import nescala.SessionCookie
import unfiltered.response.Html5

trait Templates {
  def indexPage(session: Option[SessionCookie] = None) = Html5(
    <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <meta name="viewport" content="width=device-width,initial-scale=1"/>
        <title>&#8663;northeast scala symposium</title>
        <link href="http://fonts.googleapis.com/css?family=Source+Code+Pro|Montserrat:400,700|Open+Sans:300italic,400italic,700italic,400,300,700" rel="stylesheet" type="text/css"/>
        <link rel="stylesheet" type="text/css" href="/css/gridism.css" />
        <link rel="stylesheet" type="text/css" href="/css/normalize.css" />
        <link href="//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css" rel="stylesheet"/>
        <link href="/css/font-mfizz/font-mfizz.css" rel="stylesheet"/>
        <link rel="stylesheet" type="text/css" href="/css/2015/style.css" />
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
        <script type="text/javascript"
          src={"https://maps.googleapis.com/maps/api/js?key=AIzaSyASm3CwaK9qtcZEWYa-iQwHaGi3gcosAJc&sensor=false"}></script>
        <script type="text/javascript" src="/js/2015/index.js"></script>
      </head>
      <body class="wrap wider">
        <div class="unit whole center-on-mobiles top section">
          <h1>
            The <a href="http://scala-lang.org/" target="_blank"><strong>red stripes</strong></a><br/>
            are coming<br/>
            to <strong>Boston</strong>
          </h1>
          <hr/>
          <div>northeast scala symposium 2015</div>
          <hr/>
          <div class="grid">
            <div class="unit half right center-on-mobiles">
              <p>
                RSVP on <a href="http://www.meetup.com/nescala/">Meetup</a>
              </p>
              <p>
                <a href="http://www.meetup.com/nescala/events/218741329/">Day one</a> | <a href="http://www.meetup.com/nescala/events/218741348/">Day two</a>
              </p>
            </div>
            <div class="left unit half center-on-mobiles">
              <p>
                Presenter topic submissions
              </p>
              <p>
                Open soon
              </p>
            </div>        
          </div>
          <div class="communicate">
            <a class="icon" href="http://twitter.com/nescalas" target="_blank"><i class="fa fa-twitter"></i><span>Listen for the bird call</span></a>
            <a href="http://www.meetup.com/nescala/" target="_blank" class="icon"><i class="icon-scala"></i><span>Join our community</span></a>
            <a href="#what" class="icon"><i class="fa fa-check-circle-o"></i><span>Listen to your peers</span></a>
            <a href="#when" class="icon"><i class="fa fa-calendar-o"></i><span>Mark your calendar</span></a>
            <a href="#where" class="icon"><i class="fa fa-map-marker"></i><span>Align your compass</span></a>
          </div>
        </div>
        <div class="inverse" id="what">
          <div class="grid">
            <div class="unit">
              <h2><strong>Conference</strong> meets <strong>community</strong></h2>
              <p>
                Northeast Scala Symposium is a <a href="http://scala-lang.org/">Scala</a>-focused <strong>community</strong> gathering.
              </p>
              <p>
                A uniquely-blended programming language deserves a uniquely-blended conference format. NE Scala offers a mix of speaker-oriented conference presentations with unconference-style sessions and discussions. All presenters are attendees and all attendees select presenters.
              </p>
            </div>
          </div>
        </div>
        <div id="when">
          <div class="grid">
            <div class="unit">
              <h2>When</h2>
              <p>
                Northeast Scala Symposium is held annually. In 2015, we will occupy the greater Boston area on <a href="http://www.meetup.com/nescala/events/218741329/">Friday, January 30</a> and <a href="http://www.meetup.com/nescala/events/218741348/">Saturday, January 31</a>.
              </p>
              <p>
                Attendance requires a RSVP.
              </p>
            </div>
          </div>
        </div>
        <div class="inverse" id="where">
          <div class="grid">
            <div class="unit whole">
              <h2>Where</h2>
              <p>
                This year's symposium will be held at <a href="http://districthallboston.org/spaces/">District Hall</a>, located in the <a href={"https://www.google.com/maps?f=q&hl=en&q=75+Northern+Ave.,+Boston,+MA,+us"}>heart of Boston</a>, a city where early Puritan colonists hid <a href="http://www.mbta.com/uploadedfiles/Documents/Schedules_and_Maps/Rapid%20Transit%20w%20Key%20Bus.pdf">lambdas</a> in the public transit system.
              </p>
            </div>
          </div>
        </div>
        <div class="unit whole center-on-mobiles">
          <div id="map"></div>
        </div>
        <div id="kindness">
          <div class="grid">
            <div class="unit whole">
              <h2>Be kind.</h2>
              <p>
                Nobody likes a jerk, so <strong>show respect</strong> for those around you.
              </p>
              <p>
                NE Scala is dedicated to providing a harassment-free experience for everyone, regardless of gender, gender identity and expression, sexual orientation, disability, physical appearance, body size, race, or religion (or lack thereof). We do not tolerate harassment of participants in any form.
              </p>
              <p>
                All communication should be appropriate for a technical audience including people of many different backgrounds. Sexual language, innuendo, and imagery is not appropriate for any symposium venue, including talks.
              </p>
              <p>
                Participants violating these rules may be asked to leave without a refund at the sole discretion of the organizers.
              </p>
              <p>
                Since this is a gathering of static typists, offenders will be caught at compile time.
              </p>
            </div>
          </div>
        </div>
        <footer class="unit whole center-on-mobiles">
          <hr/>
          <a href="#top">NE Scala</a> is made possible with <span class="love">❤</span> from the
          <div>
            <a href="http://www.meetup.com/boston-scala/">Boston</a>,
            <a href="http://www.meetup.com/scala-phase/">Philadelphia</a>,
            and <a href="http://www.meetup.com/ny-scala/">New York</a> scala enthusiasts and, of course, of all of
            <a href="http://www.meetup.com/nescala/photos/">you</a>.
          </div>
          <div>
            hosting by the fine folks @ <a href="https://www.heroku.com/">Heroku</a>
          </div>
        </footer>
      </body>
    </html>
  )
}

