package com.meetup.boston

trait Templates extends com.meetup.Templates {
  def bostonLayout(head: xml.NodeSeq)
    (bodyScripts: xml.NodeSeq)
    (body: xml.NodeSeq) = unfiltered.response.Html(
      <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <title>&#8663;northeast scala symposium</title>
        <link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Arvo:regular,bold"/>
        <link rel="stylesheet" type="text/css" href="/css/tipsy.css" />
        <link rel="stylesheet" type="text/css" href="/facebox/facebox.css"/>
        <link rel="stylesheet" type="text/css" href="/css/boston.css" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
        { head }
      </head>
      <body>
        { body }
        <div id="footer">
          made possible with <span class="love">&#10084;</span> from the <a href="http://groups.google.com/group/boston-scala">Boston</a>, <a href="http://www.meetup.com/scala-phase/">Philadelphia</a>, and <a href="http://www.meetup.com/ny-scala">New York</a> scala enthusiasts
          <div id="lets">2012 let's make more awesome</div>
        </div>
      <script type="text/javascript" src="/facebox/facebox.js"></script>
      <script type="text/javascript" src="/js/jquery.tipsy.js"></script>
      { bodyScripts }
      </body>
    </html>
  )

  def indexNoAuth = index(false)

  def indexWithAuth = index(true)

  private def index(authed: Boolean) = bostonLayout(Nil)(
    <script type="text/javascript" src="/js/boston.js"></script>)(
    <div id="head" class="clearfix">
      <div class="contained">
        <div class="l">
          <h1>&#8663;northeast<span>scala</span>symposium</h1>
        </div>
        <div class="r">
          <h2>Boston</h2>
          <h4>functionally typed party</h4>
        </div>
      </div>
    </div>
    <div id="day-one" class="day clearfix">
      <div class="contained">
        <div id="talk-submissions">
          <div class="l">
            <h1>Day One</h1>
            <h2>3.09.11</h2>
            <h3>
              <span>9am @<a href="http://www.meetup.com/nescala/events/37637442/">NERD</a></span>
            </h3>
            <p>Scala Talks</p>
          </div>
          <div class="r">
            <h1>Submit a Talk</h1>
            <p>The Northeast Scala Symposium features talks by members of the Boston, New York, and Philadelphia Scala Meetups, and by guests from far afield. All talks are selected in advance by attendees.</p>
            <p>We have space for 16 talks of 30 minutes, and one keynote talk. Speakers may propose one talk on the topic of their choice. Whichever talk accrues the most votes will be the keynote, and this speaker will receive 45 minutes to talk as well as $1000 to offset travel expenses.</p>
            {
              if(authed) {
                <a href="#" id="propose-talk">Propose a talk</a>
                <div id="propose-form">
                  <form action="POST">
                    <div>
                      <label for="name">What's your talk called?</label>
                      <input type="text" name="name" maxlenth="200" placeholder="How I learned to love my type system" />
                    </div>
                    <div>
                      <label for="description">What's your talk is about?</label>
                      <div class="limited">
                        <textarea name="description" data-limit="600" placeholder="Say it in 600 characters or less" />
                        <div><div class="limit-label"/> <input type="submit" value="Propose Talk" class="btn" /></div>
                      </div>
                    </div>
                  </form>
                </div>
              } else { <p><a href="/connect">Log into Meetup</a> to submit a talk.</p> }
            }
          </div>
        </div>
      </div>
    </div>
    <div id="day-two" class="day clearfix">
      <div class="contained">
        <div class="l">
          <h1>Day Two</h1>
          <h2>3.10.11</h2>
          <h3>
            <span>10am @<a href="http://www.meetup.com/nescala/events/44042982/">Stata Center</a></span>
          </h3>
          <p>Scala Workshops</p>
        </div>
        <div class="r">
          <p>The second day of the symposium is hands-on Scala hacking and workshops hosted at MIT.</p>
        </div>
      </div>
    </div>
    <div id="day-three" class="day clearfix">
      <div class="contained">
        <div class="l">
          <h1>Day Three</h1>
          <h2>3.11.11</h2>
          <h3>
            <span>10am @<a href="http://www.meetup.com/nescala/events/44049692/">Stata Center</a></span>
          </h3>
        </div>
        <div class="r">No details yet</div>
      </div>
    </div>
  )
}

object Templates extends Templates {}
