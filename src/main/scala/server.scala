package nescala

object Server {
  import unfiltered.jetty._
  import unfiltered.filter._
  import unfiltered.response.ResponseString
  def main(args: Array[String]) {
    Http(Option(System.getenv("PORT")).getOrElse("8080").toInt)
    .resources(getClass().getResource("/www"))
    .filter(Planify {
      NESS.site orElse boston.Boston.site orElse nyc.Nyc.site orElse (
        boston.Proposals.intent orElse boston.Panels.intent orElse(
          boston.Boston.talks orElse boston.Boston.api
        )
      )
    }).run(
      _ => (),
      _ => dispatch.Http.shutdown()
    )
  }
}
