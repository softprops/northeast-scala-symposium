package nescala.boston

import nescala.Store
object Reify {
  def main(a: Array[String]) {
    
    val Proposal = """boston:proposals:(.*):(.*)""".r
    val PanelProposal = """boston:panel_proposals:(.*):(.*)""".r

    val keynote = "boston:proposals:12229286:12"
    val talks = Seq(
      "boston:proposals:4414303:20",
      "boston:proposals:12169933:45",
      "boston:proposals:8958946:43",
      "boston:proposals:4283131:9",
      "boston:proposals:13110932:38",
      "boston:proposals:9314821:10",
      "boston:proposals:3982342:39",
      "boston:proposals:13631141:32",
      "boston:proposals:9569760:37",
      "boston:proposals:32879542:14",
      "boston:proposals:10646327:21",
      "boston:proposals:7230113:41",
      "boston:proposals:13267841:34",
      "boston:proposals:10987883:25",
      "boston:proposals:3025132:48",
      "boston:proposals:6251692:22"
    )
    val panel = "boston:panel_proposals:11785524:1"

    Store { s =>
      keynote match {
        case was @ Proposal(mid, _) =>
          var kn = "boston:keynote:%s" format mid
          if(!s.exists(kn)) {
            s.hgetall[String, String](was) match {
              case Some(value) =>
                println("setting keynote %s to %s" format(kn, value))
                s.hmset(kn, value)
              case _ => println("no value associated with %s" format was)
            }
          } else {
            println("keynote %s already exists" format kn)
          }
      }

      talks.foreach { _ match {
        case was @ Proposal(mid, _) =>
          val tk = "boston:talks:%s" format mid
          if(!s.exists(tk)) {
            s.hgetall[String, String](was) match {
              case Some(value) =>
                println("setting talk %s to %s" format(tk, value))
                s.hmset(tk, value)                
              case _ => println("no value associated with %s" format was)
            }
          } else {
            println("talk %s already exists" format tk)
          }
      } }


      panel match {
        case was @ PanelProposal(mid, _) =>
          var pk = "boston:panel:%s" format mid
          if(!s.exists(pk)) {
            s.hgetall[String, String](was) match {
              case Some(value) =>
                println("setting panel %s to %s" format(pk, value))
                s.hmset(pk, value)                
              case _ => println("no value associated with %s" format was)
            }
          } else {
            println("panel %s already exists" format pk)
          }
      }

    }
  }
}
