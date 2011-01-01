package com.meetup

object Twitter extends JsonCached with Config {
  import dispatch._
  import twitter._
  import dispatch.liftjson.Js._
  import Http._

  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._

  implicit def http = new dispatch.AppEngineHttp

  def tweets =
    cacheOr("tweets", "current") {
      val twts = http(Search("#nescala"))
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
