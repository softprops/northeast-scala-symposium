package nescala.philly

trait SponsorTemplate { self: Templates =>
  def sponsors =
    phillyLayout(Nil)(
    <script type="text/javascript" src="/js/jquery.scrollTo-1.4.2-min.js"></script>
    <script type="text/javascript" src="/js/boston/index.js"></script>)(
      head(true/*hide login*/) ++ 
      <div id="sponsors">
        <h1>A Little Help From Our Friends</h1>
      </div>
    )
}
