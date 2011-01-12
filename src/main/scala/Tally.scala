package com.meetup

import models.Vote
import scala.util.Random
import unfiltered.request._
import unfiltered.response._
import dispatch.oauth.Token
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import scala.collection.JavaConversions._
import java.lang.{Integer => JInt}

object Tally extends Templates with Entries {
  def intent: unfiltered.filter.Plan.Intent = {
    case _ =>
      val tallied: Map[Int, List[Int]] = Storage { mgr =>
        val query = mgr.newQuery(classOf[Vote])
        val votes = query.execute().asInstanceOf[java.util.List[Vote]]
        (((Map.empty[Int, List[Int]] /: (0 until entries.size))((m,i)=>
          m + (i -> Nil))) /: votes)((m, v) =>
            m + (v.entry_id -> (v.member_id :: m(v.entry_id))))
      }

      // val max = (tallied.toList max Ordering[Int].on[(_,List[Int])](_._2.size))._2.size
      val total = (0 /: tallied.values) (_ + _.size)

      page(<h2>Tally Ho!</h2><ul data-total={total.toString} id="tallies">{ tallied flatMap {
        case (e, members) =>
          <li title={entries(e).speaker} id={"e-%s" format e} data-score={((members.size.toDouble / total) * 100).toString}>
            <span class="bar">.</span>
            <span class="title">{entries(e).title} <strong>{members.size}</strong></span>
          </li>
      }}</ul>)

  }

  def page = layout(<link href="/css/tally.css" type="text/css" rel="stylesheet"/>)(<script type="text/javascript" src="js/jquery.tipsy.js"></script><script type="text/javascript" src="/js/tally.js"></script>)_
}
