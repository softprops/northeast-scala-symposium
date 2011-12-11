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
        <div id="container">
          { body }
          <div id="footer">
            made possible with <span class="love">&#10084;</span> from the <a href="http://groups.google.com/group/boston-scala">Boston</a>, <a href="http://groups.google.com/group/scala-phase">Philadelphia</a>, and <a href="http://www.meetup.com/ny-scala">New York</a> scala enthusiasts
          <div id="lets">2012 let's make more awesome</div>
        </div>
      </div>
      <script type="text/javascript" src="/facebox/facebox.js"></script>
      <script type="text/javascript" src="/js/jquery.tipsy.js"></script>
      { bodyScripts }
      </body>
    </html>
  )

  def index = bostonLayout(Nil)(Nil)(
    <div id="head" class="clearfix">
      <div class="l">
        <h1>&#8663;northeast<span>scala</span>symposium</h1>
      </div>
      <div class="r">
        <h1>Boston</h1>
        <h2>3/9/11</h2>
        <h3>
          <span>9am @<a href="http://www.meetup.com/nescala/events/37637442/">NERD</a></span>
        </h3>
      </div>
    </div>
    <div id="talk-submissions">
      <h1>Submit a Talk</h1>
      <p>The Northeast Scala Symposium features talks by members of the Boston, New York, and Philadelphia Scala Meetups, and by guests from far afield. All talks are selected in advance by attendees.</p>
      <p>We have space for 16 talks of 30 minutes, and one keynote talk. Speakers may propose one talk on the topic of their choice. Whichever talk accrues the most votes will be the keynote, and this speaker will receive 45 minutes to talk as well as $1000 to offset travel expenses.</p>
      <p>Please provide a single-paragraph description of your proposed talk. All submissions will be presented for voting; the only requirement is that you authenticate with a Meetup account. Speakers may enter Twitter usernames and other biographical information in their <a href="http://www.meetup.com/nescala/profile/">member profile page</a>.</p>
     </div>
  )
}

object Templates extends Templates {}
