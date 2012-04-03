package nescala.boston

import nescala.Meetup

trait Templates extends nescala.Templates with SponsorTemplate {
  import java.net.URLEncoder.encode

  def bostonLayout(head: xml.NodeSeq)
    (bodyScripts: xml.NodeSeq)
    (body: xml.NodeSeq) = unfiltered.response.Html5(
      <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <title>&#8663;northeast scala symposium</title>
        <link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Arvo:regular,bold"/>
        <link rel="stylesheet" type="text/css" href="/css/tipsy.css" />
        <link rel="stylesheet" type="text/css" href="/facebox/facebox.css"/>
        <link rel="stylesheet" type="text/css" href="/css/boston.css" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
        { head }
      </head>
      <body>
        { body }
        <div id="footer">
          made possible with <span class="love">&#10084;</span> from the <div><a href="http://www.meetup.com/boston-scala/">Boston</a>, <a href="http://www.meetup.com/scala-phase/">Philadelphia</a>, and <a href="http://www.meetup.com/ny-scala/">New York</a> scala enthusiasts,</div>
        <div class="divided">hosting from <a href="http://www.heroku.com/">heroku</a></div>
        <div>and <a href="/2012/friends">with a little help from our friends</a>.</div>
          <div id="last-year">
            <div>What happen to last year? It ended.</div>
            <div>But you can still find it <a href="/2011">here</a>.</div>
            <a href="/2011"><img src="/images/ne.png"/></a>
          </div>
          <div id="lets">2012 let's make more awesome</div>
        </div>
      <script type="text/javascript" src="/facebox/facebox.js"></script>
      <script type="text/javascript" src="/js/jquery.tipsy.js"></script>
      <script type="text/javascript" src="/js/boston/boston.js"></script>
      { bodyScripts ++ embedly }
      </body>
    </html>
  )

  def tallied(authed: Boolean, total: Int, entries: Seq[Map[String, String]], kind: String) =
    bostonLayout(
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

  def panelList(props: Seq[Map[String, String]]) =
    listOf(props, "panel_proposals", Panels.MaxTalkName,
           Panels.MaxTalkDesc, "Your panel proposals", "Edit Panel", "#propose-panel-form")

  def proposalList(props: Seq[Map[String, String]]) =
    listOf(props, "proposals", Proposals.MaxTalkName,
           Proposals.MaxTalkDesc, "Your talk proposals", "Edit Talk", "#propose-form")

  def listOf(props: Seq[Map[String, String]], kind: String, maxName: Int,
             maxDesc: Int, listTitle: String, editLabel: String, sourceForm: String) =
    <div id={ kind }>
      <h5 class="proposal-header">{ listTitle }</h5>
       <ul id={ "%s-list" format kind }>
       {
         props.map { p =>
         <li id={ p("id") }>
           <form action={"/boston/%s/%s" format (kind, encode(p("id"), "utf8")) }
                    method="POST" class="propose-edit-form">
             <div>
               <a href="#" class="toggle name" data-val={ p("name") }>{ p("name") }</a>
               <input type="text" name="name" maxlength={ maxName + "" } value={ p("name") } />
             </div>
             <div class="preview">
               <div class="controls clearfix">
                 <ul>
                   <li>
                    <a href="mailto:doug@meetup.com">Email us</a> if you wish to withdraw this.
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
                    <div class="limit-label"/>
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
    <a href="https://twitter.com/nescalas" class="twitter-follow-button" data-show-count="false" data-lang="en" data-size="large">Follow @nescalas</a>
  }

  def login(authed: Boolean, then: String) =
    if(!authed) <div id="auth-bar" class="clearfix"><div class="contained"><div class="l">Just who are you anyway?</div><div class="r"><a href={ "/connect%s" format(if(then.isEmpty) "" else "?then=%s".format(then)) } class="btn login">Log in with Meetup</a></div></div></div> else <span/>

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
      <div class="l">
        <a href="/"><h1>&#8663;northeast<span>scala</span>symposium</h1></a>
      </div>
      <div class="r">
        <h2>Boston</h2>
        <h4>functionally typed party</h4>
        { twttrFollow }
      </div>
    </div>
   </div> ++ { login(authed, afterlogin) }

  // listing of talk proposals (refactor plz)
  def talkListing(
    proposals: Seq[Map[String, String]],
    authed: Boolean = false,
    votes: Seq[String] = Seq.empty[String]) = bostonLayout(Nil)(Nil)({
      head(true/*hide login*/, "vote-for-talk")
    } ++ <div class="contained">
     <div id="maybe-talks-header">
        <h2>{ proposals.size } Scala campfire stories</h2>
        <div>This year's symposium will feature 16 talks and one <a href="/2012/panels">panel</a> from members of the Scala community. Below is a list of current talk proposals.</div>
      </div>
      <ul>{
        proposals.map { p =>
        <li class="talk" id={ p("id").split(":")(3) }>
          <h2><a href={ "#"+p("id").split(":")(3) }>{ p("name") }</a></h2>
          <div class="who-box clearfix">
            <img class="avatar" src={ p("mu_photo").replace("member_", "thumb_") } />
            <div class="links">
              <a class="primary" href={ "http://meetup.com/nescala/members/%s" format p("id").split(":")(2) } target="_blank">{ p("mu_name") } </a>{ if(p.isDefinedAt("twttr")) {
                  <a class="twttr" href={ "http://twitter.com/%s" format p("twttr").drop(1) } target="_blank">{ p("twttr") }</a>
                } else <span/> }
            </div>
          </div>
          <p class="desc">{ p("desc") }</p>
        </li>
        }
      }</ul>
    </div>
  )

  // listing of panel proposals (refactor plz)
  def panelListing(proposals: Seq[Map[String, String]], authed: Boolean = false,
                   votes: Seq[String] = Seq.empty[String]) = bostonLayout(Nil)(Nil)({
    head(true/*hide login*/, "vote-for-panel")
  } ++ <div class="contained">
      <div id="maybe-talks-header">
        <h2>{ proposals.size } Scala Panel { if(proposals.size == 1) "Discussion" else "Discussons" }</h2>
        <div>In addition to a number of <a href="/2012/talks">talks</a>, this year's symposium will feature one panel discussion among peers. Below is a list of current panel proposals.</div>
      </div>
      <ul>{
        proposals.map { p =>
        <li class="talk" id={ p("id").split(":")(3) }>
          <h2><a href={ "#"+p("id").split(":")(3) }>{ p("name") }</a></h2>
          <div class="who-box clearfix">
            <img class="avatar" src={ p("mu_photo").replace("member_", "thumb_") } />
            <div class="links">
              <a class="primary"
                 href={ "http://meetup.com/nescala/members/%s" format p("id").split(":")(2) }
                 target="_blank">{ p("mu_name") }
              </a>{ if(p.isDefinedAt("twttr")) {
                <a class="twttr" href={ "http://twitter.com/%s" format p("twttr").drop(1) } target="_blank">{ p("twttr") }</a>
              } else <span/> }
            </div>
          </div>
          <p class="desc">{ p("desc") }</p>
        </li>
        }
      }</ul>
    </div>
  )

  val rsvps =
    <div class="attending">
      <h4 class="tban"/><ul class="rsvps"/><p class="extra-rsvps"/>
    </div>

 def dayOne(authed: Boolean, keynote: Map[String, String], talks: Seq[Map[String, String]], panel: Map[String, String]) = 
   <div id="day-one" data-event={ Meetup.Boston.dayone_event_id } class="day clearfix">
      <div class="contained">
        <div id="talk-submissions">
          <div class="l">
            <h1><a href="http://www.meetup.com/nescala/events/37637442/">Day 01</a></h1>
            <h2>3.09.12</h2>
            <h3>
              <span>9am @<a href="http://maps.google.com/maps?q=One+Memorial+Drive%2C+Cambridge%2C+MA">NERD</a></span>
            </h3>
            <p>Scala Talks and Panel Discussion</p>
            <p><a href="http://www.google.com/calendar/render?cid=https%3A%2F%2Fwww.google.com%2Fcalendar%2Ffeeds%2F7eirtkhjnn83vudugebkqpr3uc%2540group.calendar.google.com%2Fpublic%2Fbasic" target="_blank"><img src="http://www.google.com/calendar/images/ext/gc_button6.gif" border="0" alt="0" style="max-height: 700px; max-width: 700px; margin: 5px;" /></a></p>
          </div>
          <div class="r">
            <h1>Votes are in</h1>
            <p>This year's symposium features 16 talks of 30 minutes, one keynote talk, and one 45 - 60 minute panel discussion.</p>
            <p>Thanks to all attendees who voted for their favorite talks and panels.</p>
          </div>
          <div class="l hl">
            <h3>Keynote</h3>
          </div>
          <div class="r hl">
          </div>
          <div class="talk l" id="keynote">
            <h3>
              <a href="#keynote">{ keynote("name") }</a>
            </h3>
            <div class="who-box clearfix">
              <img class="avatar" src={ keynote("mu_photo").replace("member_", "thumb_") } />
              <div class="links">
                <a class="primary"
                   href={ "http://meetup.com/nescala/members/%s" format keynote("id").split(":")(2) }
                   target="_blank">{ keynote("mu_name") }
                </a>{ if(keynote.isDefinedAt("twttr")) {
                <a class="twttr"
                    href={ "http://twitter.com/%s" format keynote("twttr").drop(1) }
                    target="_blank">{ keynote("twttr") }
                </a>
                } else <span/> }
              </div>
            </div>
          </div>
          <div class="r">
            <div><a class="vid" href="http://www.youtube.com/watch/?v=YZxL0alO1yc">video</a></div>
            <div class="desc">{ keynote("desc").trim() }</div>
          </div>
          <div class ="l hl"><h3>Talks</h3></div>
          <div class="r hl"></div>{ talks.map { t =>
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
                 } else <span/> } { if(t.isDefinedAt("slides")) {
                   <a href={ t("slides") }>slides</a>
                 } else <span/> }
              </div>
            </div>
          </div>
          
          <div class="r desc">
            {if(t.isDefinedAt("video")) { <a class="vid" href={ t("video").toString }>video</a> } }
            <p>{ t("desc").trim() }</p>
          </div>
          } }
          <div class="l hl">
            <h3>Panel</h3>
          </div>
          <div class="r hl">
          </div>
          <div id="panel" class="talk l">
            <h3><a href="#panel">{ panel("name") }</a></h3>
            <div class="who-box clearfix">
              <img class="avatar" src={ panel("mu_photo").replace("member_", "thumb_") } />
              <div class="links">
                <a class="primary" href={ "http://meetup.com/nescala/members/%s" format panel("id").split(":")(2) } target="_blank">{ panel("mu_name") } </a>{ if(panel.isDefinedAt("twttr")) {
                <a class="twttr" href={ "http://twitter.com/%s" format panel("twttr").drop(1) } target="_blank">{ panel("twttr") }</a>
                } else <span/> }
              </div>
            </div>
          </div>
          <div class="r">
            <p class="desc">{ panel("desc").trim() }</p>
          </div>{
             rsvps
           }
        </div>
      </div>
    </div>

  private val dayTwo =
    <div id="day-two" data-event={ Meetup.Boston.daytwo_event_id } class="day clearfix">
      <div class="contained">
        <div class="l">
          <h1><a href="http://www.meetup.com/nescala/events/44042982/" target="_blank">Day 02</a></h1>
          <h2>3.10.12</h2>
          <h3>
            <span>10am @<a href="http://maps.google.com/maps?q=32+Vassar+Street%2C+Cambridge%2C+MA">Stata Center</a></span>
          </h3>
          <p>Scala Workshops</p>
          
        </div>
        <div class="r">
          The second day of the symposium is a hands-on unconference of Scala coding workshops held at MIT's Stata Center.
          <p>
            We've got room for 30 Scala Workshops, in five different rooms and 6 timeslots. The unconference orientation and planning is 9-10 in the morning. We can't serve food in this space so please arrive caffeinated and ready to workshop.
          </p>
          <p>
            There will be two morning sessions, DIY lunch 12-1:30, and four afternoon sessions. Anyone can organize a workshop. The best workshops will be highly interactive and educational.
          </p>
        </div>
        { rsvps }
      </div>
    </div>

  private val dayThree =
    <div id="day-three" data-event={ Meetup.Boston.daythree_event_id }
      class="day clearfix">
      <div class="contained">
        <div class="l">
          <h1><a href="http://www.meetup.com/nescala/events/44049692/">Day 03</a></h1>
          <h2>3.11.12</h2>
          <h3>
            <span>10am @<a href="http://maps.google.com/maps?q=32+Vassar+Street%2C+Cambridge%2C+MA">Stata Center</a></span>
          </h3>
          <p>Hack day</p>
          
        </div>
        <div class="r">No formal events are planned, but space at the Stata Center will be available for attendees to meet, talk, and hack.</div>
        { rsvps }
      </div>
    </div>

  def index(
    authed: Boolean,
    keynote: Map[String, String],
    talks: Seq[Map[String, String]],
    panel: Map[String, String]) =
    bostonLayout(Nil)(
    <script type="text/javascript" src="/js/jquery.scrollTo-1.4.2-min.js"></script>
    <script type="text/javascript" src="/js/boston/index.js"></script>)(
      head(true/*hide login*/) ++ dayOne(authed, keynote, talks, panel) ++ dayTwo ++ dayThree
    )
}

object Templates extends Templates {}
