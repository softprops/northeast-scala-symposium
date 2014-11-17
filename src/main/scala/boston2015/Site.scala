package nescala.boston2015

import unfiltered.request.{ Path, Seg }
import unfiltered.response.Html5
import unfiltered.Cycle

object Site extends Templates {
  def pages: Cycle.Intent[Any, Any] = {
    case Path(Seg(Nil)) => indexPage
  }
}

trait Templates {
  def indexPage = Html5(
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
      </head>
      <body class="wrap wider">
        <div class="unit whole center-on-mobiles top">
          <h1>
            The <a href="http://scala-lang.org/" target="_blank"><strong>red stripes</strong></a><br/>
            are coming<br/>
            to <strong>Boston</strong>
          </h1>
          <hr/>
          <div>northeast scala symposium 2015</div>
          <hr/>
          <div class="communicate">
            <a class="icon" href="http://twitter.com/nescalas" target="_blank"><i class="fa fa-twitter"></i><span>Listen for the bird call</span></a>
            <a href="http://www.meetup.com/nescala/" target="_blank" class="icon"><i class="icon-scala"></i><span>Join our community</span></a>
          </div>
        </div>
        <footer class="unit whole center-on-mobiles">
          <a href="#top">nescala</a> is made possible with <span class="love">❤</span> from the
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
