package nescala.philly

import nescala.Meetup
import scala.collection.immutable.TreeMap

trait Templates extends nescala.Templates with SponsorTemplate {
  import java.net.URLEncoder.encode

  val meetupGroup = "http://www.meetup.com/nescala/"
  val eventLink = meetupGroup + "events/" + Meetup.Philly.eventId + "/"

  def phillyLayout(head: xml.NodeSeq)
    (bodyScripts: xml.NodeSeq)
    (body: xml.NodeSeq) = unfiltered.response.Html5(
      <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <title>&#8663;northeast scala symposium</title>
        <link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Arvo:regular,bold"/>
        <link rel="stylesheet" type="text/css" href="/css/tipsy.css" />
        <link rel="stylesheet" type="text/css" href="/facebox/facebox.css"/>
        <link rel="stylesheet" type="text/css" href="/css/philly.css" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
        { head }
      </head>
      <body>
        { body }
        <div id="footer">
          made possible with <span class="love">&#10084;</span> from the <div><a href="http://www.meetup.com/boston-scala/">Boston</a>, <a href="http://www.meetup.com/scala-phase/">Philadelphia</a>, and <a href="http://www.meetup.com/ny-scala/">New York</a> scala enthusiasts,</div>
        <div class="divided">hosting from <a href="http://www.heroku.com/">heroku</a></div>
        <div>and <a href="/2013/friends">with a little help from our friends</a>.</div>
          <div id="last-year">
            <div>What happen to last year? It ended.</div>
            <div>But you can still find it <a href="/2012">here</a>.</div>
            <a href="/2011"><img src="/images/ne.png"/></a>
          </div>
          <div id="lets">2013 let's make more awesome</div>
        </div>
      <script type="text/javascript" src="/facebox/facebox.js"></script>
      <script type="text/javascript" src="/js/jquery.tipsy.js"></script>
      <script type="text/javascript" src="/js/philly/philly.js"></script>
      { bodyScripts ++ embedly }
      </body>
    </html>
  )

  def tallied(authed: Boolean, total: Int, entries: Seq[Map[String, String]], kind: String) =
    phillyLayout(
      <script type="text/javascript" src="/js/tally.js"></script>)(<link rel="stylesheet" type="text/csss" href="/css/tally.css"/>)({
        head(authed, kind)
      } ++ <div class="contained"> { if(authed) {
         <p><strong>{total}</strong> votes submitted so far</p>
         <ul data-total={ total.toString } id="tallies">{ entries map { e =>
           <li class="clearfix" title={ e.get("mu_name").getOrElse("mu_name") } id={"e-%s" format e("id") }
            data-score={ ((e("votes").toDouble / total) * 100).toString }>
            <img class="avatar" src={  e("mu_photo").replace("member_", "thumb_") } />
            <div class="progress clearfix"><span class="bar">.</span>
            <span class="title">{ e("name") } <strong>{ e("votes") }</strong></span></div>
          </li>
        } }</ul> } else {
          <p>No Peeking. Login to view tally</p>
       } }</div>)


  def proposalList(props: Seq[Map[String, String]]) =
    listOf(props, "proposals", Proposals.MaxTalkName,
           Proposals.MaxTalkDesc, "Your talk proposals", "Edit Talk", "#propose-form")

  def listOf(props: Seq[Map[String, String]], kind: String, maxName: Int,
             maxDesc: Int, listTitle: String, editLabel: String, sourceForm: String) =
    <div id={ kind }>
      <h5 class="proposal-header">{ listTitle }</h5>
       <ul id={ "%s-list" format kind }>{ if (props.isEmpty) { <li id="no-proposals" class="instruct">None yet</li>} }
       {
         props.map { p =>
         <li id={ p("id") }>
           <form action={"/philly/%s/%s" format (kind, encode(p("id"), "utf8")) }
                    method="POST" class="propose-edit-form">
             <div>
               <a href="#" class="toggle name" data-val={ p("name") }>{ p("name") }</a>
               <input type="text" name="name" maxlength={ maxName + "" } value={ p("name") } />
             </div>
             <div class="preview">
               <div class="controls clearfix">
                 <ul>
                   <li>
                    <a href={"mailto:doug@meetup.com?subject=please withdraw talk %s" format p("id") }>Email us</a> if you wish to withdraw this.
                   </li>
                   <li>
                      <a href="#" class="edit-proposal" data-proposal={ p("id") }>
                        edit
                      </a>
                    </li>
                 </ul>
               </div>
               <div class="linkify desc" data-val={ p("desc") }>{ p("desc") }</div>
               <div class="edit-desc limited">
                  <textarea data-limit={ maxDesc + "" } name="desc">{ p("desc") }</textarea>
                  <div class="form-extras">
                    <div class="limit-label"></div>
                    <div class="edit-controls clearfix">
                      <input type="submit" value={ editLabel } class="btn" />
                      <input type="button" value="Cancel" class="btn cancel" />
                    </div>
                  </div>
                </div>
             </div>
           </form>
         </li>
         }
       }
     </ul>
    </div>

  val twttrFollow = {
    <a href="https://twitter.com/nescalas" class="twitter-follow-button" data-show-count="false" data-size="large">Follow @nescalas</a>
  }

  def login(authed: Boolean, after: String = "") = <span class="none"></span>
    /*if(!authed) <div id="auth-bar" class="clearfix"><div class="contained"><div class="l">Just who are you anyway?</div><div class="r"><a href={ "/login%s" format(if(after.isEmpty) "" else "?then=%s".format(after)) } class="btn login">Log in with Meetup</a></div></div></div> else <span/>*/

  val embedly =
    <script type="text/javascript"><![CDATA[
      var embedly_maxWidth = 550;
      var embedly_method = 'replace';
      var embedly_wrapElement = 'div';
      var embedly_className = 'embed';
      var embedly_addImageStyles = true;
      var embedly_cssSelector = 'a.vid';
    ]]></script><script type="text/javascript" src="http://scripts.embed.ly/embedly.js"></script>;

  def head(authed: Boolean, afterlogin: String = "") =
   <div id="head" class="clearfix">
    <div class="contained">
      <a id="home-link" href="/" class="clearfix">
        <div>
          <h1>ne<span>sc</span></h1>
          <h1><span>ala</span>s</h1>
          <h1>ympo</h1>
          <h1>sium</h1>
        </div>
        <img src="/images/bell.png" id="bell"/>
      </a>
      <div>
        <h4>Scala community liberation</h4>
        <h4>in <a href="/#where">Philly</a></h4>
        <h4>Feb 8-9, 2013</h4>
        <hr/>
      </div>
    </div>
   </div> ++ { login(authed, afterlogin) }

  def talkListing(
    authed: Boolean,
    proposals: Seq[Map[String, String]],
    canVote: Boolean = false,
    votes: Seq[String] = Seq.empty[String]) = phillyLayout(
    <script type="text/javascript" src="/js/philly/voting.js"></script>)(Nil)({
      head(canVote, "vote")
    } ++ <div class="contained">
     <div id="maybe-talks-header">
        <h2 id="proposed">{ proposals.size } Scala campfire stories</h2>
        <div>
          <p>This year's symposium features talks from members of the Scala community. Below is a list of this years talk proposals.</p>
        </div>{ if (canVote) <div id="votes-remaining">You have { Votes.MaxTalkVotes - votes.size match {
          case 0 => " no votes"
          case 1 => " one vote"
          case n => " %d votes" format n
        } } remaining</div> }
      </div>
      <ul>{
        proposals.map { p =>
        <li class="talk" id={ p("id").split(":")(3) }>
          <h2><a href={ "#"+p("id").split(":")(3) }>{ p("name") }</a></h2>
          <div class="who-box clearfix">
            <div style={ "background:url(%s);" format(p("mu_photo").replace("member_", "thumb_")) }>
               <img class="avatar" src={ p("mu_photo").replace("member_", "thumb_") } />
            </div>
            <div class="links">
              <a class="primary" href={ "http://meetup.com/nescala/members/%s" format p("id").split(":")(2) } target="_blank">{ p("mu_name") } </a>{ if(p.isDefinedAt("twttr")) {
                  <a class="twttr" href={ "http://twitter.com/%s" format p("twttr").drop(1) } target="_blank">{ p("twttr") }</a>
                } else <span></span> }
            </div>
            { if (canVote) {
              <div>
                <form class="ballot" action="/philly/votes" method="POST">
                  <input type="hidden" name="vote" value={ p("id") }/>
                  <input type="hidden" name="action" value={ if (votes.contains(p("id"))) "unvote" else "vote" }/>
                  <input type="submit" class={ "voting btn%s" format(if (votes.contains(p("id"))) " voted-yes" else "") }
                    value={ if(votes.contains(p("id"))) "Withdraw Vote" else "Vote" } disabled={
                      if(votes.size >= Votes.MaxTalkVotes && !votes.contains(p("id"))) Some(xml.Text("disabled")) else None } />
                </form>
              </div>
            } }
          </div>
          <p class="desc">{ p("desc") }</p>
        </li>
        }
      }</ul>
    </div>
  )

  val rsvps =
    <div class="attending">
      <h4 class="tban"></h4><ul class="rsvps"></ul><p class="extra-rsvps"></p>
    </div>

  val drexelMap = "http://goo.gl/maps/s0n7h"

  def blurb(authed: Boolean) =
    <div id="blurb">
      <div class="contained">
      <p>Just <a href={ eventLink }>RSVP</a> <span class="amp">&amp;</span> you're in.</p>
      <hr/>
      { twttrFollow }
      </div>
    </div>

  val times = Map(
    "registration" -> "9:00am",
    "opening" -> "9:55am",
    "keynote" -> "10:00am",
    "0"       -> "11:00am",
    "1"       -> "11:45am",
    "2"       -> "12:30",
    "lunch"   -> "1:15pm",
    "3"       -> "2:15pm",
    "4"       -> "3:00pm",
    "5"       -> "3:45am",
    "6"       -> "4:30pm",
    "7"       -> "5:15pm")

  val tracks = Map(
    "0" -> "A",
    "1" -> "B"
  )

  def renderRegistration = {
    <div class="l hl">
      <span class="time">{ times("registration") }</span>
      <h3>Registration</h3>
    </div>
    <div class="r hl"></div>
    <div class="l"></div>
    <div class="r">
      Sign in and get something to eat for breakfast provided by <a href="https://www.novus.com/" target="_blank"><img class="sponsor-inline" alt="novus" src="/images/sponsors/novus-bw.svg"/></a>
    </div>
  }

  def renderOpeningRemarks = {
    <div class="l hl">
      <span class="time">{ times("opening") }</span>
      <h3>Remarks</h3>
    </div>
    <div class="r hl"></div>
    <div class="l"></div>
    <div class="r">
      Opening remarks and symposium kick off
    </div>
  }

  def renderLunch = {
    <div class="l hl">
      <span class="time">{ times("lunch") }</span>
      <h3>Lunch</h3>
    </div>
    <div class="r hl"></div>
    <div class="l"></div>
    <div class="r">
      Refuel with lunch provided by <a href="http://www.linkedin.com/" target="_blank"><img alt="linkedin" class="sponsor-inline linkedin" src="/images/sponsors/linkedin-bw.svg"/></a>
    </div>
  }


  def youtube(url: String) = {
    val YouTube = """http(s?)://www.youtube.com/watch\?v=(.+)""".r
    url match {
      case YouTube(_, id) =>
        <iframe width="550" height="309" src={"http://www.youtube.com/embed/%s?feature=oembed".format(id)} frameborder="0" allowfullscreen=""></iframe>
      case _ =>
        <a class="vid" href={ url }>video</a>
    }
  }

  def renderKeynote(keynote: Map[String, String]) = {
    val memberId = keynote("id").split(":")(3)
    <div class="l hl">
      <span class="time">{times("keynote")}</span>
      <h3>Keynote</h3>
    </div>
    <div class="r hl"></div>
    <div class="talk l" id="keynote">
      <h3>
        <a href="#keynote">{ keynote("name") }</a>
      </h3>
      <div class="who-box clearfix">
        <img class="avatar" src={ keynote("mu_photo").replace("member_", "thumb_") } />
        <div class="links">
          <a class="primary"
              href={ "http://meetup.com/nescala/members/%s" format memberId }
              target="_blank">{ keynote("mu_name") }
          </a>{ if(keynote.isDefinedAt("twttr")) {
          <a class="twttr"
             href={ "http://twitter.com/%s" format keynote("twttr").drop(1) }
              target="_blank">{ keynote("twttr") }
           </a>
          } else <span></span> } { if (keynote.isDefinedAt("slides")) {
              <a href={ keynote("slides") }>slides</a>
            } else <span></span> }
        </div>
      </div>
    </div>
    <div class="r">
      {if(keynote.isDefinedAt("video")) { youtube(keynote("video")) ++ <hr/> } }
      <div class="desc">{ keynote("desc").trim() }</div>
    </div>
  }

  def renderTalk(t: Map[String, String]): xml.NodeSeq = {
    val memberId = t("id").split(":")(3)
    val track = tracks(t.getOrElse("track", "0"))
    <div class="talk l" id={ "t-" + memberId }>
      <div class={"track track%s" format track.toLowerCase }>{ track }</div>
      <h3><a href={ "#t-" + memberId }>{ t("name") }</a></h3>
        <div class="who-box clearfix">
          <img class="avatar"
              src={ t("mu_photo").replace("member_", "thumb_") } />
          <div class="links">
            <a class="primary"
                href={ "http://meetup.com/nescala/members/%s" format memberId }
                target="_blank">{ t("mu_name") }
            </a>{ if(t.isDefinedAt("twttr")) {
            <a class="twttr"
                href={ "http://twitter.com/%s" format t("twttr").drop(1) }
                target="_blank">{ t("twttr") }</a>
            } else <span></span> } { if(t.isDefinedAt("slides")) {
              <a href={ t("slides") }>slides</a>
            } else <span></span> }
          </div>
        </div>
      </div>
      <div class="r desc">
        {if(t.isDefinedAt("video")) { youtube(t("video")) ++ <hr/> } }
        <p>{ t("desc").trim() }</p>
      </div>
      <hr/>
  }

  def renderTalkOrder(order: String, talks: Seq[Map[String, String]]) = {
    <div class ="l hl">
      <span class="time">{ times(order) }</span>
      <h3>Talks</h3>
    </div>
    <div class="r hl"></div><div>{
      talks.map(renderTalk)
    }</div> ++ { if (order == "2") renderLunch  else <span></span> }
  }

  def renderTalks(talks: Seq[Map[String, String]]): xml.NodeSeq =
    (TreeMap.empty[String, Seq[Map[String, String]]] ++
       talks.sortBy(t => (t("order"), t("track")))
       .groupBy(_("order")))
       .map { case (ord, ts) => renderTalkOrder(ord, ts) }
       .toList
       .flatten

  def dayOne(authed: Boolean,
             keynote: Map[String, String],
             talks: Seq[Map[String, String]],
             proposals: Seq[Map[String, String]]): xml.NodeSeq = {
   <div id="day-one" class="clearfix">
      <div class="contained">
        <div id="speaking" class="clearfix">
         <div class="inner-right">
          <div class="l">
            <h1>Schedule</h1>
          </div>
          <div class="r">
            <p>This year's symposium features two tracks of eight <span>{Proposals.TalkTime}</span> minute talks and one 45 minute keynote.</p>
            <hr/>
            <p>Thanks to all the attendees who voted on talk selections.</p>
          </div>
          { renderRegistration }
          { renderOpeningRemarks }
          { renderKeynote(keynote) }
          { renderTalks(talks) }
        </div>
        <div class="inner-left"></div>
      </div>
      <div id="where" class="clearfix">
        <div class="l divy">
          <h1>Arriving</h1>
        </div>
        <div class="r divy">
          <div id="venue-image-container"></div>
          <p>This year's symposium will be held at Drexel University.</p>
          <p>For more information on finding hotels check out <a href="http://universitycity.org/accommodations">this list of accommodations</a> or suggest one <a href="http://www.meetup.com/nescala/events/97192402/comments/142822472/">here</a>.
          </p>
        </div>
      </div>
      <div class="day clearfix divy" data-event={ Meetup.Philly.eventId.toString }>
       { rsvps }
      </div>
    </div>
  </div>
  }

  def propose(proposals: Seq[Map[String, String]]): xml.NodeSeq =
    if (proposals.isEmpty) <div class="l"></div> else {
    <div class="l" id="proposal-notifications">
      <div id="proposal-notification"></div>
      <div id="proposal-edit-notification"></div>
    </div>
    <div class="r" id="propose-talk">
      <h2>Speak up</h2>
      <p class="instruct">
        Thanks for submitting your talk proposals.
      </p>
      <p class="instruct">
        You may enter your Twitter username and other biographical
        information on your
        <a target="_blank" href="http://www.meetup.com/nescala/profile/">
          Meetup member profile
        </a>.
      </p>
      <div id="propose-container" class="divy">
        { proposalList(proposals) }
      </div>
    </div>
  }

  def showTalk(t: Map[String, String]) = {
    <div class="talk l" id={ "t-" +t("id").split(":")(2) }>
      <h3>
        <a href={ "#t-"+t("id").split(":")(2) }>{ t("name") }</a>
      </h3>
      <div class="who-box clearfix">
        <img class="avatar"
          src={ t("mu_photo").replace("member_", "thumb_") } />
        <div class="links">
          <a class="primary"
            href={ "http://meetup.com/nescala/members/%s" format t("id").split(":")(2) }
            target="_blank">{ t("mu_name") }
          </a>{ if(t.isDefinedAt("twttr")) {
            <a class="twttr"
              href={ "http://twitter.com/%s" format t("twttr").drop(1) }
              target="_blank">{ t("twttr") }</a>
            } else <span></span> } { if (t.isDefinedAt("slides")) {
              <a href={ t("slides") }>slides</a>
            } else <span></span> }
          </div>
        </div>
      </div>
      <div class="r desc">
        {if(t.isDefinedAt("video")) { youtube(t("video")) ++ <hr/> } }
        <p>{ t("desc").trim() }</p>
      </div>
  }


  def indexPage(
    authed: Boolean,
    keynote: Map[String, String] = Map.empty[String,String],
    talks: Seq[Map[String, String]] = Nil,
    proposals: Seq[Map[String, String]] = Nil) =
    phillyLayout(Nil)(
    <script type="text/javascript" src="/js/jquery.scrollTo-1.4.2-min.js"></script>
    <script type="text/javascript" src="/js/philly/index.js"></script>)(
      head(authed) ++ blurb(authed) ++ dayOne(authed, keynote, talks, proposals)
    )
}

object Templates extends Templates {}
