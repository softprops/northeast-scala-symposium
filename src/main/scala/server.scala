package com.meetup

object Server {
  import unfiltered.jetty._
  import unfiltered.filter._
  import unfiltered.response.ResponseString
  def main(args: Array[String]) {
    Http(8080)
    .resources(getClass().getResource("/www"))
    .filter(ny.App).run
  }
}
