package nescala.philly

trait SponsorTemplate { self: Templates =>
  def sponsors =
    phillyLayout(Nil)(
    <script type="text/javascript" src="/js/jquery.scrollTo-1.4.2-min.js"></script>
    <script type="text/javascript" src="/js/boston/index.js"></script>)(
      head(true/*hide login*/) ++ 
      <div id="sponsors">
        <h1>A Little Help From Our Friends</h1>
        <hr/>
        <div class="sponsor">
          <a href="https://www.novus.com/"><img src="/images/sponsors/novus.jpg" /></a>
          <p>Novus is making sure everyone starts nescala out right by helping out with a well-balanced breakfast.</p>
        </div>
        <hr/>
        <div class="sponsor">
          <a href="http://www.linkedin.com/"><img src="/images/sponsors/linkedin.png" /></a>
          <p>Linkedin has linked us up with help to feed all attendees lunch.</p>
        </div>
        <hr/>
        <div class="sponsor">
          <a href="http://www.heroku.com/"><img src="/images/sponsors/heroku.png" /></a>
          <p>Heroku makes nescala.org possible and for that we are thankful.</p>
        </div>      
        <hr/>
        <div class="sponsor">
          <a href="http://www.icn-i.com/"><img src="/images/sponsors/intelcapnet.png" /></a>
          <p>Intelligent Capital Network is making sure attendees are happy by buying drinks for happy hour.</p>
        </div>      
        <hr/>
      </div>
    )
}
