package nescala

import models.Vote
import scala.util.Random
import unfiltered.request._
import unfiltered.response._
import dispatch.oauth.Token
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import scala.collection.JavaConversions._
import java.lang.{Integer => JInt}

object Tally extends Templates with nyc.Entries {
  def intent: unfiltered.Cycle.Intent[Any, Any] = {
    case _ =>
      /*val tallied: Map[Int, List[Int]] = Storage { mgr =>
        val query = mgr.newQuery(classOf[Vote])
        val votes = query.execute().asInstanceOf[java.util.List[Vote]]
        (((Map.empty[Int, List[Int]] /: (0 until entries.size))((m,i)=>
          m + (i -> Nil))) /: votes)((m, v) =>
            m + (v.entry_id -> (v.member_id :: m(v.entry_id))))
      }

      val ordered = (tallied.toList sortBy(_._2.size)).reverse

      val total = (0 /: ordered) (_ + _._2.size)

      page(<h2>Tally Ho!</h2>
        <ul data-total={total.toString} id="tallies">{ ordered flatMap {
          case (id, votes) =>
            <li title={entries(id).speaker} id={"e-%s" format id}
              data-score={((votes.size.toDouble / total) * 100).toString}>
              <span class="bar">.</span>
              <span class="title">{entries(id).title} <strong>{votes.size}</strong></span>
          </li>
        }}</ul>)*/

      ResponseString("used to be tally page")
  }

  def page = layout(<link href="/css/tally.css" type="text/css" rel="stylesheet"/>)(
    <script type="text/javascript" src="js/jquery.tipsy.js"></script>
    <script type="text/javascript" src="/js/tally.js"></script>)_
}
