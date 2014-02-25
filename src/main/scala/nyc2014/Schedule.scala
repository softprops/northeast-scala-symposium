package nescala.nyc2014

import java.util.{ Calendar, Date, TimeZone }

sealed trait Slot {
  def time: Date
  def title: String
  def content: String
}

case class NonPresentation(time: Date, title: String, content: String) extends Slot

case class Presentation(proposal: Proposal) extends Slot {
  val time = proposal.time.getOrElse(new Date)
  val title = proposal.name
  val content = proposal.desc
}

case class Schedule(slots: Seq[Slot])

object Schedule {
  def get = {
    val cal = {
      val c = Calendar.getInstance()
      c.setTimeZone(TimeZone.getTimeZone(
        "US/Eastern"))
      c
    }
    
    def timeAt(hour: Int, min: Int = 0) = {
      cal.set(2014, 3, 2, hour, min, 0)
      cal.getTime
    }      

    val slots = (Seq(
      NonPresentation(
        timeAt(8), "Registration", "Grab a coffee and a nametag. Then grab more coffee. Then grab a seat."),
      NonPresentation(
        timeAt(9), "Welcome", "Opening remarks and class begins."),
      NonPresentation(
        timeAt(12, 30), "Lunch", "Disperse for food."),
      NonPresentation(
        timeAt(18), (
          <a href="https://www.google.com/maps/place/110+Crosby+St/@40.7241392,-73.9968689,17z/data=!3m1!4b1!4m2!3m1!1s0x89c2598f108d4b09:0xc6a04d369a17b693">Foursquare Happy Hour</a>
        ).toString, "Happy times.")) ++
      Proposals.talks.map(Presentation(_)))
        .sortBy(_.time)
    
    Some(Schedule(slots))
  }
}
