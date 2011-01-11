package com.meetup

object Twitter extends JsonCached with Config {
  import dispatch._
  import twitter._
  import dispatch.liftjson.Js._
  import Http._
  import oauth._
  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._
  import java.util.logging.Logger

  private final val log = Logger.getLogger(getClass.getName)

  implicit def http = new dispatch.AppEngineHttp
  val consumer = Consumer(property("twttr_consumer"), property("twttr_consumer_secret"))
  val token = Token(property("twttr_token"), property("twttr_token_secret"))

  def rate_limit =
    http(Account.rate_limit_status_as(consumer, token)) match {
      case js =>
        val RateLimitStatus.remaining_hits(hits) = js
        val RateLimitStatus.reset_time_in_seconds(resetSecs) = js
        val RateLimitStatus.hourly_limit(lim) = js
        val RateLimitStatus.reset_time(resetTime) = js
        (hits, resetSecs, lim, resetTime)
    }

  def tweets =
    cacheOr("tweets", "current") {
      val twts: List[dispatch.json.JsObject] = try {
        http(Search("#nescala") user_agent("Dispatch-nescala/1.0"))
      } catch { case _ =>
        log.warning("Exception occured in twitter search api call, this client may be past its rate limit")
        Nil
      }
      val (hits, resetSecs, lim, resetTime) = rate_limit
      log.info("Twitter API rate limit status. remaining: %s, reset time in secs: %s, hourly limit: %s reset time: %s".format(
        hits, resetSecs, lim, resetTime
      ))
      val result: List[(Option[BigDecimal], Option[String], Option[String], Option[String])] = try { for {
        t <- twts
      } yield {
        (Search.id.unapply(t), Search.from_user.unapply(t), Search.text.unapply(t), Search.created_at.unapply(t))
      } } catch { case _ => Nil }
      (result map {
       case (id, user, twt, time) =>
         ("id" -> id) ~ ("user" -> user) ~ ("text" -> twt) ~ ("created_at" -> time)
      }, Some(System.currentTimeMillis + intProperty("ttl")))
    }
}
