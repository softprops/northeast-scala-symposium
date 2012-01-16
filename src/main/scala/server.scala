package nescala

object Server {
  import unfiltered.jetty._
  import unfiltered.filter._
  import unfiltered.response.ResponseString
  def main(args: Array[String]) {
    Http(Option(System.getenv("PORT")).getOrElse("8080").toInt)
    .resources(getClass().getResource("/www"))
    .filter(Planify {
      (NESS.site /: Seq(boston.Boston.site, nyc.Nyc.site, boston.Proposals.intent,
                        boston.Panels.intent, boston.Boston.talks, boston.Boston.panels,
                        boston.Votes.intent, boston.Boston.api, boston.Tally.talks,
                        boston.Tally.panels))(_ orElse _)
    }).run(
      _ => (),
      _ => dispatch.Http.shutdown()
    )
  }
}
