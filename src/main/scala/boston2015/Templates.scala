package nescala.boston2015

import nescala.SessionCookie
import unfiltered.response.Html5
import java.net.URLEncoder.encode

trait Templates {

  /** list of current member's proposals and modal for editing them */
  private def proposed
   (proposals: Iterable[Proposal]): xml.NodeSeq =
    (<h3 id="proposed">Your current talk proposals</h3><ul>{
      proposals.map { p =>
        <li>
         <p>
          <a href="#" class="btn propose"
           data-id={p.id} data-kind={p.kind}
           data-name={p.name} data-desc={p.desc}><i class="fa fa-pencil-square-o"></i> Edit</a>
           { p.kind match {
             case "medium"    =>
               <span class="mute"> 45 min</span>
             case "short"     =>
               <span> 30 min</span>
             case "lightning" =>
               <span> 15 min</span>
           } } <i class="fa fa-clock-o"></i>
          <a href={s"#${p.domId}"} title={p.name}> { p.name } </a>
         </p>
        </li>
      }
    }</ul>
   )

  /** every proposal related thing owned by the current member */
  private def propose(proposals: Iterable[Proposal]): xml.NodeSeq = (<section>
    <div class="grid center-on-mobiles" id="propose">
      <div class="unit whole">
       <p>
         Attending NE Scala is only half the experience.
       </p>
       <p>
        Attendees are encouraged so submit talk proposals that other attendees can vote on.
        You can submit up to <strong>3 talk proposals</strong>. If you're shy, don't be afraid.
        A 15 minute lightning talk goes by faster
        than you think. If you're used to talking for hours, remember to time yourself.
       </p>
      </div>
      <div id="propose-talk">
        <div id="propose-container" class="unit">
          { proposals.size match {
            case 0 =>
              <span>
                <p>
                 <a href="#" class="btn propose">Submit a new talk proposal</a>
                </p>
              </span>
            case n if n < Proposal.Max =>
              <span>
                { proposed(proposals) }
                <p><a href="#" class="btn propose">Submit a new talk proposal</a></p>
               </span>
            case n =>
              <span>
                { proposed(proposals) }
              </span>
          } }
        <div class="modal">
         <div class="grid form"></div>
        </div>
        </div>
      </div>
    </div>
  </section>)

  def proposalsPage
   (proposals: Iterable[Proposal])
   (session: Option[SessionCookie] = None) =
    layout(session)(scripts = Seq("/js/2015/proposals.js"))(
      <div class="unit whole align-center lead inverse">
        <div class="grid">
          <p class="unit whole">
          This year's symposium borrows from last year's mix of talk lengths in order to give
          new attendees a chance to speak up and share what's on their mind.
          </p>
        </div>
        <div class="grid">
          <div class="unit one-third">
            <a href="#medium-proposals">Med</a>
            (45 min)
            <p class="small mute">3 slots</p>
          </div>
          <div class="unit one-third">
            <a href="#medium-proposals">Short</a>
            (30 min)
            <p class="small mute">4 slots</p>
          </div>
          <div class="unit one-third">
            <a href="#lightning-proposals">Lightning</a>
            (15 min)
            <p class="small mute">6 slots</p>
          </div>        
        </div>
      </div>)(<div class="grid center" id="speak">
       <div class="unit whole">
         <h2>
           <i class="fa fa-bullhorn"></i> Speak up
         </h2>
       </div>
       <div class="unit whole">
      { session match {
          case Some(member) if member.nescalaMember =>
            propose(member.proposals)
          case Some(member) =>
            <p>
              You can be part of this year's symposium by joining our <a href="http://www.meetup.com/nescala/">Meetup group</a>
            </p>
          case _ =>
            <p>
              You can be part of this year's symposium by joining our <a href="http://www.meetup.com/nescala/">Meetup group</a>
              and <a href="/login?state=propose">logging in</a> to vote or to submit a talk.
            </p>            
        } }
        </div>
      </div>
      <div class="inverse whole" id="proposals">
        <div class="grid">
        <div class="unit whole">
          <h2>
           <i class="fa fa-check-circle-o"></i> Listen up
          </h2>
          <p>
            NE Scala does not select speakers for you to watch and listen to, you do. Talks
            are proposed by your peers and in a short time we will open up voting polls for you
            to select the talks you want to see.
          </p>
        </div>
        <div class="unit whole">{
          proposals.groupBy(_.kind).map {
            case (kind, ps) =>
              <h3 id={s"$kind-proposals"} class="unit whole">
                {ps.size} <strong>{ kind }</strong> length proposals
              </h3>
              <ul>{ ps.map(proposal(false, Nil)) }</ul>
          }
        }</div>
      </div></div>)

  private def personal(p: Proposal): xml.NodeSeq = avatar(p) ++ links(p)

  private def avatar(p: Proposal) =
    <a href={ s"http://meetup.com/nescala/members/${p.memberId}"}
      class="circle" style={s"background-image:url(${p.member.get.photo}); background-size: cover; background-position: 50%"}>
    </a>

  private def links(proposal: Proposal) =
    (<div class="links">
     <a class="primary" href={ s"http://meetup.com/nescala/members/${proposal.memberId}" } target="_blank">
       { proposal.member.get.name }
     </a>
      {
        if (proposal.member.get.twttr.isDefined) (
          <p>
            <i class="fa fa-twitter"></i>
            <a class="twttr small" href={ s"http://twitter.com/${proposal.member.get.twttr.get.drop(1)}"} target="_blank">
             { proposal.member.get.twttr.get }
            </a>
          </p>
        )
      }
    </div>)

   /** a single li element for a proposal */
   private def proposal
    (canVote: Boolean, votes: Seq[String])(p: Proposal)  =
    <li class="unit whole talk" id={ p.domId }>
    <div class="grid">
      <div class="unit one-fifth">
        { personal(p) }
      </div>
      <div class="unit four-fifths">
        <h2><a href={ "#"+p.domId }>{ p.name }</a></h2>
        <div>
          <span class="mute"><i class="fa fa-clock-o"></i> { p.kind match {
            case "medium" => "45 minutes"
            case "short" => "30 minutes"
            case "lightning" => "15 minutes"
          } }
          </span>
          { if (canVote && votes.contains(p.id))
            <form class="ballot" action="/2014/votes" method="POST">
             <input type="hidden" name="vote" value={ p.id }/>
             <input type="hidden" name="action" value={ if (votes.contains(p.id)) "unvote" else "vote" }/>
             <input type="submit" class="voting btn"
              value="This got your vote" disabled="disabled"/>
            </form>
          }
        </div>
        <p class="desc">{ p.desc }</p>
      </div>
    </div>
    <hr/>
  </li>

  def indexPage(session: Option[SessionCookie] = None) =
    layout(session)(scripts = Seq(
      "https://maps.googleapis.com/maps/api/js?key=AIzaSyASm3CwaK9qtcZEWYa-iQwHaGi3gcosAJc&sensor=false",
      "/js/2015/index.js"))(
      <div class="grid">
            <div class="unit half right center-on-mobiles">
              <p>
                RSVP on <a href="http://www.meetup.com/nescala/">Meetup</a>
              </p>
              <p>
                <a href="http://www.meetup.com/nescala/events/218741329/">Day one</a> |
                <a href="http://www.meetup.com/nescala/events/218741348/">Day two</a>
              </p>
            </div>
            <div class="left unit half center-on-mobiles">
              { session match {
                case Some(member) if member.nescalaMember =>
                  <p class="pushdown">
                    <a href="/2015/talks#speak" class="btn">Propose a talk</a>
                  </p>
                case Some(member) =>
                  <p class="pushdown">
                    Join our <a href="http://www.meetup.com/nescala">Meetup group</a> to submit a proposal.
                  </p>
                case _ =>
                  <p class="pushdown">
                    <a href="/login?state=propose" class="btn">Login</a> to submit a talk proposal
                  </p>
              }}
            </div>
          </div>
          <div class="communicate">
            <a class="icon" href="http://twitter.com/nescalas" target="_blank"><i class="fa fa-twitter"></i>
               <span>Listen for our call</span>
            </a>
            <a href="http://www.meetup.com/nescala/" target="_blank" class="icon"><i class="icon-scala"></i><span>Join our community</span></a>
            <a href="#what" class="icon"><i class="fa fa-check-circle-o"></i><span>Listen to your peers</span></a>
            <a href="#when" class="icon"><i class="fa fa-calendar-o"></i><span>Mark your calendar</span></a>
            <a href="#where" class="icon"><i class="fa fa-map-marker"></i><span>Align your compass</span></a>
          </div>
    )(<div class="inverse" id="what">
          <div class="grid">
            <div class="unit">
              <h2><strong>Conference</strong> meets <strong>community</strong></h2>
              <p>
                Northeast Scala Symposium is a <a href="http://scala-lang.org/">Scala</a>-focused <strong>community</strong> gathering.
              </p>
              <p>
                A uniquely-blended programming language deserves a uniquely-blended conference format. NE Scala offers a mix of speaker-oriented conference presentations with unconference-style sessions and discussions. All presenters are attendees and all attendees select presenters.
              </p>
            </div>
          </div>
        </div>
        <div id="when">
          <div class="grid">
            <div class="unit">
              <h2>When</h2>
              <p>
                Northeast Scala Symposium is held annually. In 2015, we will occupy the greater Boston area on <a href="http://www.meetup.com/nescala/events/218741329/">Friday, January 30</a> and <a href="http://www.meetup.com/nescala/events/218741348/">Saturday, January 31</a>.
              </p>
              <p>
                Attendance requires an RSVP.
              </p>
            </div>
          </div>
        </div>
        <div class="inverse" id="where">
          <div class="grid">
            <div class="unit whole">
              <h2>Where</h2>
              <p>
                This year's symposium will be held at <a href="http://districthallboston.org/spaces/">District Hall</a>, located in the <a href={"https://www.google.com/maps?f=q&hl=en&q=75+Northern+Ave.,+Boston,+MA,+us"}>heart of Boston</a>, a city where early Puritan colonists hid <a href="http://www.mbta.com/uploadedfiles/Documents/Schedules_and_Maps/Rapid%20Transit%20w%20Key%20Bus.pdf">lambdas</a> in the public transit system.
              </p>
            </div>
          </div>
        </div>
        <div class="unit whole center-on-mobiles">
          <div id="map"></div>
        </div>
        <div id="kindness">
          <div class="grid">
            <div class="unit whole">
              <h2>Be kind.</h2>
              <p>
                Nobody likes a jerk, so <strong>show respect</strong> for those around you.
              </p>
              <p>
                NE Scala is dedicated to providing a harassment-free experience for everyone, regardless of gender, gender identity and expression, sexual orientation, disability, physical appearance, body size, race, or religion (or lack thereof). We do not tolerate harassment of participants in any form.
              </p>
              <p>
                All communication should be appropriate for a technical audience including people of many different backgrounds. Sexual language, innuendo, and imagery is not appropriate for any symposium venue, including talks.
              </p>
              <p>
                Participants violating these rules may be asked to leave without a refund at the sole discretion of the organizers.
              </p>
              <p>
                Since this is a gathering of static typists, offenders will be caught at compile time.
              </p>
            </div>
          </div>
        </div>)

  def layout
   (session: Option[SessionCookie] = None)
   (scripts: Iterable[String] = Nil,
    styles: Iterable[String] = Nil)
   (headContent: xml.NodeSeq)
   (bodyContent: xml.NodeSeq) = Html5(
    <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <meta name="viewport" content="width=device-width,initial-scale=1"/>
        <title>&#8663;northeast scala symposium</title>
        <link href="http://fonts.googleapis.com/css?family=Source+Code+Pro|Montserrat:400,700|Open+Sans:300italic,400italic,700italic,400,300,700" rel="stylesheet" type="text/css"/>
        <link rel="stylesheet" type="text/css" href="/css/gridism.css" />
        <link rel="stylesheet" type="text/css" href="/css/normalize.css" />
        <link href="//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css" rel="stylesheet"/>
        <link href="/css/font-mfizz/font-mfizz.css" rel="stylesheet"/>
        <link rel="stylesheet" type="text/css" href="/css/2015/style.css" />
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>{
          styles.map { s => <link rel="stylesheet" type="text/css" href={s}/> } ++
          scripts.map { s => <script type="text/javascript" src={s}></script> }
        }
        <script type="text/javascript" src=""></script>
      </head>
      <body class="wrap wider">
        <div class="unit whole center-on-mobiles top section">
          <h1>
            The <a href="http://scala-lang.org/" target="_blank"><strong>red stripes</strong></a><br/>
            are coming<br/>
            to <strong>Boston</strong>
          </h1>
          <hr/>
          <div><a href="/">northeast scala symposium 2015</a></div>
          <hr/>
          { headContent }
        </div>
        { bodyContent }
        <footer class="unit whole center-on-mobiles">
          <hr/>
          <a href="#top">NE Scala</a> is made possible with <span class="love">❤</span> from the
          <div>
            <a href="http://www.meetup.com/boston-scala/">Boston</a>,
            <a href="http://www.meetup.com/scala-phase/">Philadelphia</a>,
            and <a href="http://www.meetup.com/ny-scala/">New York</a> scala enthusiasts and, of course, of all of
            <a href="http://www.meetup.com/nescala/photos/">you</a>.
          </div>
          <div>
            hosting by the fine folks @ <a href="https://www.heroku.com/">Heroku</a>
          </div>
        </footer>
      </body>
    </html>
  )
}
