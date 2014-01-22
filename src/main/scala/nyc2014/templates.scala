package nescala.nyc2014

import nescala.Meetup
import java.net.URLEncoder.encode

trait Templates {

  val meetupGroup = "http://www.meetup.com/nescala/"
  val dayoneLink = s"${meetupGroup}events/${Meetup.Nyc2014.dayoneEventId}/"
  val daytwoLink = s"${meetupGroup}events/${Meetup.Nyc2014.daytwoEventId}/"

  def btn(link: String, display: String) = <a href={link} class="button">{display}</a>

  def indexPage
    (authed: Boolean,
     keynote: Map[String, String] = Map.empty[String,String],
     talks: Seq[Map[String, String]] = Nil,
     proposals: Seq[Map[String, String]] = Nil) =
    layout(Nil)(
    <script type="text/javascript" src="/js/jquery.scrollTo.min.js"></script>
    <script type="text/javascript" src="/js/nyc2014/nyc.js"></script>
    <script type="text/javascript" src="/js/nyc2014/index.js"></script>)(
      head(authed) ++ blurb(authed) ++ proposing(authed, proposals) ++ where
    )

  def talkListing
    (authed: Boolean,
     proposals: Seq[Map[String, String]],
     canVote: Boolean = false,
     votes: Seq[String] = Seq.empty[String]) = layout(Nil
    /*<script type="text/javascript" src="/js/nyc2014/voting.js"></script> */)(Nil)({
      head(canVote, "vote")
    } ++ (<section>
      <div class="grid">
        <div class="unit whole center">
          <h2>
            { proposals.size } Scala campfire { if (proposals.size == 1) "story" else "stories" } submitted so far
          </h2>
         </div>
         <div class="unit whole center lead">
          <p>
            This year's symposium features talks in three flavors:
          </p>
          <div class="grid">
            <div class="unit one-third">
              Med (45 min)
              <p class="small mute">3 slots</p>
            </div>
            <div class="unit one-third">
              Short (30 min)
              <p class="small mute">4 slots</p>
            </div>
            <div class="unit one-third">
              Lightning (15 min)
              <p class="small mute">6 slots</p>
            </div>
          </div>
        </div>
        <div class="unit whole center">{
          if (canVote) <div id="votes-remaining">You have { Votes.MaxTalkVotes - votes.size match {
            case 0 => " no votes"
            case 1 => " one vote"
            case n => " %d votes" format n
          } } remaining</div>
        }
        </div>
      </div>
    </section>
    <section>
     <div class="grid">
      <ul>{
        proposals.map { p =>
        <li class="unit whole talk" id={ p("id").split(":")(3) }>
          <div class="grid">
            <div class="unit one-fifth">
              <a href={ s"http://meetup.com/nescala/members/${p("id").split(":")(2)}"} class="circle">
                <img height="70" width="70" src={ p("mu_photo").replace("member_", "thumb_") } alt="member" />
              </a>
              <div class="links">
                <p><a class="primary" href={ s"http://meetup.com/nescala/members/${p("id").split(":")(2)}" }
                         target="_blank">{ p("mu_name") } </a></p>{ if(p.isDefinedAt("twttr")) {
                           <p><a class="twttr small" href={ s"http://twitter.com/${p("twttr").drop(1)}" } target="_blank">{ p("twttr") }</a></p>
                         } else <span></span>
                 }
              </div><div class="mute">{ p("kind") match {
                case "medium" => "45 minutes"
                case "short" => "30 minutes"
                case  "lightning" => "15 minutes"
              }}</div>{ if (canVote) {
              <div>
                     <form class="ballot" action="/2014/votes" method="POST">
                       <input type="hidden" name="vote" value={ p("id") }/>
                       <input type="hidden" name="action" value={ if (votes.contains(p("id"))) "unvote" else "vote" }/>
                       <input type="submit" class={ "voting btn%s" format(if (votes.contains(p("id"))) " voted-yes" else "") }
                         value={ if(votes.contains(p("id"))) "Withdraw Vote" else "Vote" } disabled={
                           if (votes.size >= Votes.MaxTalkVotes && !votes.contains(p("id"))) Some(xml.Text("disabled")) else None } />
                     </form>
               </div>
               } }
             </div>
             <div class="unit four-fifths">
               <h2><a href={ "#"+p("id").split(":")(3) }>{ p("name") }</a></h2>
               <p class="desc">{ p("desc") }</p>
             </div>
          </div>
          <hr/>
        </li>
        }
      }</ul>
    </div>
   </section>)
  )

  def proposing(authed: Boolean, proposals: Seq[Map[String, String]] = Nil): xml.NodeSeq =
    if (authed) propose(proposals) else <section>
      <div class="grid" id="propose">
        <div class="unit whole">
          <h2>Speak up</h2>
          <p>The deadline for submitting talk proposals is <strong>Jan 23</strong>.</p>
          <p>Speakers will be guaranteed a spot on the RSVP list, <strong>and</strong> a spot for a friend or colleague.</p>
          <p>In order to submit a talk proposal, we need to know a little more about you.</p>
          <p><a href="/login?then=talk" class="btn">Login to talk</a></p>
        </div>
      </div>
    </section>

  def propose(proposals: Seq[Map[String, String]]): xml.NodeSeq = (<section>
    <div class="grid" id="propose">
      <div class="unit whole">
        <h2>Speak up</h2>
        <p>The deadline for submitting talk proposals is <strong>Thurs Jan 23 at midnight</strong>.</p>
        <p>Speakers will be guaranteed a spot on the RSVP list, <strong>and</strong> a spot for a friend or colleague.</p>
      </div>
      <div id="propose-talk">
        <div class="unit one-third">{ if (proposals.size < Proposals.MaxProposals)
          <p class="instruct">
            Please provide a brief single-paragraph description of your proposed talk.
          </p>
          <p class="instruct">
            Speakers may enter <a href="http://www.meetup.com/account/services/">Twitter usernames</a> and other biographical
            information on their
            <a target="_blank" href="http://www.meetup.com/account/">
              Meetup member profile
            </a>.
          </p>
          <p class="instruct">
            Each attendee can post at most <strong>three</strong> talk proposals.
          </p>
        else
          <p class="instruct">You have used up all your talk proposals</p>
       }</div>
        <div id="propose-container" class="unit two-thirds">{ if (proposals.size < Proposals.MaxProposals)
          <form action="/2014/proposals" method="POST" id="propose-form" class="proposing">
            <div id="proposal-notifications">
              <div id="proposal-notification"></div>
              <div id="proposal-edit-notification"></div>
            </div>
            <div class="grid">
              <div class="unit half">
                 <label for="kind">How long do you intend to woo us?</label>
              </div>
              <div class="unit half">
                <select name="kind">
                 <option value="medium">Medium (45 min)</option>
                 <option value="short">Short (30 min)</option>
                 <option value="lightning">Lightning (15 min)</option>
                </select>
              </div>
            </div>
            <div class="grid">
              <div class="unit whole">
                 <label for="name">What's your talk called?</label>
              </div>
              <div class="unit whole">
                <input type="text" name="name" maxlength={ Proposals.MaxTalkName + "" } placeholder="How I learned to love my type system" />
              </div>
            </div>
            <div class="grid">
              <div class="unit whole">
                <label for="desc">What's your talk is about?</label>
              </div>
              <div class="unit whole">
                <div class="limited">
                  <textarea name="desc" data-limit={ Proposals.MaxTalkDesc + "" }
                    placeholder={ "Say it in %s characters or less" format(
                    Proposals.MaxTalkDesc) }></textarea>
                </div>
                <div class="limit-label"></div>
              </div>
              <input type="submit" value="Propose Talk" class="btn" />
            </div>
          </form> else proposalList(proposals) }
        </div>
      </div>{ if (proposals.size < Proposals.MaxProposals)
        <div class="unit whole">{ proposalList(proposals) }</div> else <span></span>
       }
    </div>
  </section>)

  def proposalList(props: Seq[Map[String, String]]) =
    listOf(props, "proposals", Proposals.MaxTalkName,
           Proposals.MaxTalkDesc, "Your talk proposals", "Edit Talk", "#propose-form")

  def listOf(props: Seq[Map[String, String]], kind: String, maxName: Int,
             maxDesc: Int, listTitle: String, editLabel: String, sourceForm: String) =
    <div id={ kind }>
      <h2 class="proposal-header">{ listTitle }</h2>
       <ul id={ "%s-list" format kind }>{ if (props.isEmpty) { <li id="no-proposals" class="instruct">None yet</li>} }
       {
         props.map { p =>
         <li id={ p("id") }>
           <form action={"/2014/%s/%s" format (kind, encode(p("id"), "utf8")) }
                    method="POST" class="propose-edit-form">
             <div>
               <h3>
                 <a href="#" class="toggle name" data-val={ p("name") }>{ p("name") }</a>
               </h3>
               <input type="text" name="name" maxlength={ maxName + "" } value={ p("name") } />
             </div>
             <div class="preview">
               <div class="controls clearfix">
                 <a href="#" class="edit-proposal" data-proposal={ p("id") }>
                    Make some quick changes
                  </a> or <a href={"mailto:doug@meetup.com?subject=please withdraw talk %s" format p("id") }>
                    Email us
                  </a> if you wish to withdraw this talk.
               </div>
               <div>
                 <p>
                    This is a <select class="edit-kind" name="kind" disabled="disabled">
                    <option value="medium" selected={ Option(p("kind")).filter(_ == "medium").map( _ => "selected").orNull }>Medium (45 min)</option>
                    <option value="short" selected={ Option(p("kind")).filter(_ == "short").map( _ => "selected").orNull }>Short (30 min)</option>
                    <option value="lightning" selected={ Option(p("kind")).filter(_ == "lightning").map( _ => "selected").orNull }>Lightning (15 min)</option>
                    </select> length talk.
                 </p>
               </div>
               <p class="linkify desc" data-val={ p("desc") }>{ p("desc") }</p>
               <div class="edit-desc limited">
                  <textarea data-limit={ maxDesc + "" } name="desc">{ p("desc") }</textarea>
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

  def layout(head: xml.NodeSeq)
    (bodyScripts: xml.NodeSeq)
    (body: xml.NodeSeq) = unfiltered.response.Html5(
      <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <meta name="viewport" content="width=device-width,initial-scale=1"/>
        <title>&#8663;northeast scala symposium</title>
        <link href="http://fonts.googleapis.com/css?family=Source+Code+Pro|Montserrat:700|Open+Sans:300italic,400italic,700italic,400,300,700" rel="stylesheet" type="text/css"/>
        <link rel="stylesheet" type="text/css" href="/css/gridism.css" />
        <link rel="stylesheet" type="text/css" href="/css/normalize.css" />
        <link rel="stylesheet" type="text/css" href="/css/nyc2014.css" />
        <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
        { head }
      </head>
      <body class="wrap wider" id="top">
        { body }
        <footer>
          <div class="grid">
            <div class="unit whole">
              <a href="#top">nescala</a> is made possible with <span class="love">&#10084;</span> from the
              <div>
                <a href="http://www.meetup.com/boston-scala/">Boston</a>,
                <a href="http://www.meetup.com/scala-phase/">Philadelphia</a>,
                and <a href="http://www.meetup.com/ny-scala/">New York</a> scala enthusiasts and, of course, of all of
                <a href="http://www.meetup.com/nescala/photos/">you</a>.
              </div>
            </div>
            <div class="unit whole">
              { twttrFollow }
            </div>
            <div class="unit whole">
              hosting by <a href="http://www.heroku.com/">heroku</a>
            </div>
            <div class="center">
              <div class="unit whole">
                <p>
                  This year's symposium, uses structural sharing with those, 3 years passed.
                </p>
              </div>
              <p class="unit one-third">
                <a href="/2013">2013</a>
              </p>
              <p class="unit one-third">
                <a href="/2012">2012</a>
              </p>
              <p class="unit one-third">
                <a href="/2011">2011</a>
              </p>
            </div>
          </div>
        </footer>
        <script type="text/javascript" src="/js/nyc2014/nyc.js"></script>
        { bodyScripts }
      </body>
    </html>
  )

  val twttrFollow = {
    <a href="https://twitter.com/nescalas" class="twitter-follow-button" data-show-count="false" data-size="large">Follow @nescalas</a>
  }

  /*def login(authed: Boolean, after: String = "") =
   if (!authed) <div id="auth-bar" class="unit whole">
      <div>
        <div class="l">Just who are you anyway?</div>
        <div class="r">
          <a href={ "/login%s" format(if (after.isEmpty) "" else s"?then=$after") } class="btn login">Log in with Meetup</a>
        </div>
      </div>
    </div> else <span></span> */

  def head(authed: Boolean, afterlogin: String = "") =
   <header>
    <div class="grid">
      <div class="unit whole center title">
        <a href="/" class="logo">
          <img src="/images/2014-logo.svg" alt="north east scala symposium logo"></img>
        </a>
        <div class="center">
          <hr/>
          <h4><a href="#whereone">New York, NY</a></h4>
          <h4>March 1 <span class="amp">&amp;</span> 2, 2014</h4>
        </div>
        </div>
      </div>
    </header>

  def blurb(authed: Boolean) = (<section>
    <div class="grid">
      <div class="unit whole lead center">
        <p>
          A gathering of statically refined types
        </p>
        <p class="amp">&amp;</p>
        <p>
          well-spoken peers
        </p>
        <hr/>
        <p class="small mute">
         self-selected by you, from the northeasterly regions of the US
        </p>
      </div></div></section><section><div class="grid">
      <div class="unit half">
        <h2>One day of <strong>sharing</strong>.</h2>
        <p class="mute">Sat Mar 1, 8am to 6pm</p>
        <p>
         <a href={dayoneLink}>Day 1</a> is back to basics with <a href="#whereone">one room</a>, <a href="/2014/talks">one track of talks</a>.
        </p>
        <p>
          If you wish to talk on day one, get your proposal in <a href="#propose">now</a>. The deadline for submitting talk proposals is <strong>Jan 23 at Midnight</strong>. Speakers will be guaranteed an RSVP spot, <strong>and</strong> a spot for a friend or colleague.
        </p>
        <p>
          Seating on day 1 is <strong>limited</strong>. A final block of <strong>90 spots will be released this Thursday at 2 PM</strong>. You can keep track of this status on <a href={dayoneLink}>Meetup</a>.
        </p>
        <p>
          If you've got something you'd like to talk about, <a href="#propose">let us know</a>.
        </p>
      </div>
      <div class="unit half">
        <h2>One day of <strong>pairing</strong>.</h2>
        <p class="mute">Sun Mar 2, 9am to 5pm</p>
        <p>
          <a href={daytwoLink}>Day 2</a> is a free-to-attend, hands-on <a href="http://en.wikipedia.org/wiki/Unconference">unconference</a>,
          self-organized on the spot by whoever shows up. (Trust us, this works.)
        </p>
        <p>
          In past years we've had workshops, panels, talks that didn't make it into day 1, debates, group hugs (well, not really).
          The unconference is whatever you want to make of it.
        </p>
        <p>
          We're taking over all three floors of Meetup HQ, so there will be plenty of room for everyone,
          even those who didn't get in for <a href={dayoneLink}>Day 1</a>.
        </p>
        <p>
          Unconference participants will collectively fill the schedule grid first thing Sunday morning.
          Bring your laptop, and your conversation ideas.
        </p>
       <p>
         Rooms, projectors, whiteboards, and markers will be provided. (Video adapters will not be provided! Make sure you bring your own.)
       </p>
       <p>
         There might be food; we're working on it.
       </p>
      </div>
    </div>
  </section>)

  val where = <section>
    <div class="grid">
      <div class="unit whole" id="where">
        <h2>Come. Find us.</h2>
          <p>
          This year's symposium will be held @ <a target="_blank" href="http://www.cims.nyu.edu/">Courant Institute of Mathematical Sciences</a>.
          </p>
          <div id="where-iframe"></div>
      </div>
    </div>
  </section>

  val attending = <section>
    <div class="grid">
      <div class="unit whole day" data-event={ Meetup.Nyc2014.dayoneEventId }>
        <ul class="rsvps"></ul>
      </div>
    </div>
  </section>
}

object Templates extends Templates {}

