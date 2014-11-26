package nescala.boston2015

import nescala.SessionCookie
import unfiltered.response.Html5
import java.net.URLEncoder.encode

trait Templates {

  def newProposalForm =
    <form action="/2015/talks" method="POST" id="propose-form" class="proposing">
      <div id="proposal-notifications">
        <div id="proposal-notification"></div>
        <div id="proposal-edit-notification"></div>
      </div>
      <div class="grid">
        <div class="unit half right center-on-mobiles">
          <label for="kind"><i class="fa fa-clock-o"></i> Select a talk length</label>
        </div>
        <div class="unit half left center-on-mobiles">
          <select name="kind">
            <option value="medium">Medium (45 min)</option>
            <option value="short">Short (30 min)</option>
            <option value="lightning">Lightning (15 min)</option>
          </select>
        </div>
      </div>
      <div class="grid">
        <div class="unit whole">
          <label for="name">Give your talk a good name</label>
        </div>
        <div class="unit whole">
          <input type="text" name="name" maxlength={ Proposal.MaxTalkName + "" } placeholder="How I learned to love my type system" />
        </div>
      </div>
      <div class="grid">
        <div class="unit whole">
          <label for="desc">What's your talk is about?</label>
        </div>
        <div class="unit whole">
          <div class="limited">
            <textarea name="desc" data-limit={ Proposal.MaxTalkDesc + "" }
              placeholder={ "Say it in %s characters or less" format(
                  Proposal.MaxTalkDesc) }></textarea>
          </div>
          <div class="limit-label"></div>
        </div>
        <div class="unit">
          <input type="submit" value="Propose Talk" class="btn" />
        </div>
      </div>
    </form>

  def propList(proposals: Iterable[Proposal]): xml.NodeSeq =
    (<span>{
      proposals.map { p =>
        <h3><span class="small">{ p.kind match {
          case "medium"    =>
            <span class="mute">45 min</span>
          case "short"     =>
            <span>30 min</span>
          case "lightning" =>
            <span>15 min</span>
        } } <i class="fa fa-clock-o"></i></span> { p.name } <a class="btn small">Edit</a></h3>
      }
    }</span>)

  def propose(proposals: Iterable[Proposal]): xml.NodeSeq = (<section>
    <div class="grid center-on-mobiles" id="propose">
      <div class="unit whole">
        <h2><i class="fa fa-bullhorn"></i> Speak up</h2>
      </div>
      <div id="propose-talk">
        <div id="propose-container" class="unit">
          { proposals.size match {
            case 0 =>
              <span>
                <p>Submit a new talk proposal</p>
                { newProposalForm }
              </span>
            case n if n < Proposal.Max =>
              <span>
                <p>Your current talk proposals</p>
                { propList(proposals) }
                <p>Submit a new talk proposal</p>
                { newProposalForm }
               </span>
            case n =>
              <span>
                <p>Your current talk proposals</p>
                { propList(proposals) }
              </span>
          } }
        </div>
      </div>
    </div>
  </section>)

  def proposalList(props: Iterable[Proposal]) =
    listOf(props, "proposals", Proposal.MaxTalkName,
           Proposal.MaxTalkDesc, "Your talk proposals", "Edit Talk", "#propose-form")

  def listOf(
    props: Iterable[Proposal],
    kind: String,
    maxName: Int,
    maxDesc: Int,
    listTitle: String,
    editLabel: String,
    sourceForm: String) =
    <div id={ kind }>
      <h2 class="proposal-header">{ listTitle }</h2>
       <ul id={ "%s-list" format kind }>{ if (props.isEmpty) { <li id="no-proposals" class="instruct">None yet</li>} }
       {
         props.map { p =>
         <li id={ p.id }>
           <form action={ s"/2015/$kind/${encode(p.id, "utf8")}" }
                 method="POST" class="propose-edit-form">
             <div>
               <h3>
                 <a href="#" class="toggle name" data-val={ p.name }>{ p.name }</a>
               </h3>
               <input type="text" name="name" maxlength={ maxName + "" } value={ p.name } />
             </div>
             <div class="preview">
               <div class="controls clearfix">
                 <a href="#" class="edit-proposal" data-proposal={ p.id }>
                    Make some quick changes
                  </a> or <a href={"mailto:doug@meetup.com?subject=please withdraw talk %s" format p.id }>
                    Email us
                  </a> if you wish to withdraw this talk.
               </div>
               <div>
                 <p>
                    This is a <select class="edit-kind" name="kind" disabled="disabled">
                    <option value="medium" selected={ Option(p.kind).filter(_ == "medium").map( _ => "selected").orNull }>Medium (45 min)</option>
                    <option value="short" selected={ Option(p.kind).filter(_ == "short").map( _ => "selected").orNull }>Short (30 min)</option>
                    <option value="lightning" selected={ Option(p.kind).filter(_ == "lightning").map( _ => "selected").orNull }>Lightning (15 min)</option>
                    </select> length talk.
                 </p>
               </div>
               <p class="linkify desc" data-val={ p.desc }>{ p.desc }</p>
               <div class="edit-desc limited">
                  <textarea data-limit={ maxDesc + "" } name="desc">{ p.desc }</textarea>
                  <div class="limit-label"></div>
                  <div class="form-extras">
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


  def proposalPage(session: Option[SessionCookie] = None) =
    layout(session)(scripts = Seq("/js/2015/proposals.js"))(
      <div class="unit whole center lead inverse">
        <p>
          This year's symposium borrows from last year's mix of talk lengths in order to give
          new attendees a chance to speak up and share what's on their mind.
        </p>
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
      </div>)(<div class="unit whole center">
      { session match {
          case Some(member) if member.nescalaMember =>
            <p>
              You may submit up to 3 talk proposals.
            </p> ++ { propose(member.proposals) }
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
      </div>)

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
                    <a href="/2015/talks" class="btn">Propose a talk</a>
                  </p>
                case Some(member) =>
                  <p>
                    Join our <a href="http://www.meetup.com/nescala">Meetup group</a> to submit a proposal.
                  </p>
                case _ =>
                  <p>
                    <a href="/login" class="btn">Login</a>
                    <p>to submit a talk proposal</p>
                  </p>
              }}
            </div>
          </div>
          <div class="communicate">
            <a class="icon" href="http://twitter.com/nescalas" target="_blank"><i class="fa fa-twitter"></i>
               <span>Listen for the bird call</span>
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
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>{
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
          <div>northeast scala symposium 2015</div>
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
