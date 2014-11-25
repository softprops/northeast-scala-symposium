package nescala.boston2015

import nescala.{ Store, Meetup }
import java.util.Date

case class Proposal(
  id: String,
  name: String,
  desc: String,
  kind: String,
  member: Option[Member] = None,
  time: Option[Date] = None)

object Proposal {
  def create(
    member: Member, name: String, desc: String, king: String) =
    Store { s =>
      if (!member.exists) {
        Meetup.members(Seq(member.id)).headOption.foreach {
          case mem => Member.store(
            Member(mem.id, mem.name, mem.photo,
            System.currentTimeMillis.toString, mem.twttr))          
        }        
      }
      // todo: store proposal
    }
}
