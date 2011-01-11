package com.meetup

import unfiltered.response._

object Poll {
  def intent: unfiltered.filter.Plan.Intent = {
    case CookieToken(ClientToken(v, s, Some(c))) =>
      html(<span>okay</span>)
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
}
