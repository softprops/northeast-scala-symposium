package nescala.boston

trait SponsorTemplate { self: Templates =>
  def sponsors =
    bostonLayout(Nil)(
    <script type="text/javascript" src="/js/jquery.scrollTo-1.4.2-min.js"></script>
    <script type="text/javascript" src="/js/boston/index.js"></script>)(
      head(true/*hide login*/) ++ 
      <div id="sponsors">
        <h1>A Little Help From Our Friends</h1>
        <p>If you get more than your money's worth at nescala, it's thanks to these guys.</p>
        <a href="http://www.typesafe.com/"><img src="/images/sponsors/typesafe.png" /></a>
        <p>Typesafe is hiring professionals to record the talks.</p>
        <a href="http://www.meetup.com/jobs/"><img src="/images/sponsors/meetup.png" /></a>
        <p>Meetup is making our stylish yet functional tote bags.</p>
        <a href="http://www.heroku.com/"><img src="/images/sponsors/heroku.png" /></a>
        <p>Heroku will take you into a happy hour cloud on Friday.</p>
        <a href="https://foursquare.com/jobs/"><img src="/images/sponsors/foursquare.png" /></a>
        <p>Foursquare is checking you in to free drinks on Saturday night.</p>
        <a href="http://www.ardentex.com/software.html"><img src="/images/sponsors/ardentex.png" /></a>
      </div>
    )
}
