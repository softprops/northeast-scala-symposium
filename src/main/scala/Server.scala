package nescala

import unfiltered.jetty.Http
import unfiltered.filter.Planify

object Server {
  def main(args: Array[String]) {
    Http(Option(System.getenv("PORT")).getOrElse("8080").toInt)
    .resources(getClass().getResource("/www"))
    .filter(Planify {
      (Northeast.site /: Seq(
        boston2015.Site.pages,
        nyc2014.Nyc.site,
        philly.Philly.site,
        boston.Boston.site,
        nyc.Nyc.site))(_ orElse _)
    }).run(
      _ => (),
      _ => dispatch.Http.shutdown()
    )
  }
}
