package nescala.boston

import nescala.Meetup

trait Templates extends nescala.Templates {
  import java.net.URLEncoder.encode

  def bostonLayout(head: xml.NodeSeq)
    (bodyScripts: xml.NodeSeq)
    (body: xml.NodeSeq) = unfiltered.response.Html(
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
          made possible with <span class="love">&#10084;</span> from the <a href="http://www.meetup.com/boston-scala/">Boston</a>, <a href="http://www.meetup.com/scala-phase/">Philadelphia</a>, and <a href="http://www.meetup.com/ny-scala/">New York</a> scala enthusiasts
          <div id="last-year">
            <div>What happen to last year? It ended.</div>
            <div>But you can still find it <a href="/2011">here</a>.</div>
            <a href="/2011"><img src="/images/ne.png"/></a>
          </div>
          <div id="lets">2012 let's make more awesome</div>
        </div>
      <script type="text/javascript" src="/facebox/facebox.js"></script>
      <script type="text/javascript" src="/js/jquery.tipsy.js"></script>
      { bodyScripts }
      </body>
    </html>
  )

  def indexNoAuth = index(false)

  def indexWithAuth(proposals: Seq[Map[String, String]], panels: Seq[Map[String, String]]) =
    index(true, proposals, panels)

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
                    <a href={ "/boston/%s/withdraw?id=%s" format(kind, encode(p("id"), "utf8")) }
                      data-sourceform={ sourceForm }
                      class="withdraw">withdraw</a>
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

  val head =
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
   </div>

  // listing of talk proposals
  def maybes(proposals: Seq[Map[String, String]]) = bostonLayout(Nil)(Nil)({
      head
    } ++ <div class="contained">      
      <h2 id="maybe-talks"><div>{ proposals.size } Scala</div><div class="smaller"> campfire stories</div></h2>
      <ul>{
        proposals.map { p =>
        <li class="talk" id={ p("id").split(":")(3) }>
          <h1><a href={ "#"+p("id").split(":")(3) }>{ p("name") }</a></h1>
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

  val rsvps =
    <div class="attending">
      <h4 class="tban"/><ul class="rsvps"/><p class="extra-rsvps"/>
    </div>

 def dayOne(authed: Boolean, proposals: Seq[Map[String, String]], panels: Seq[Map[String, String]]) = 
   <div id="day-one" data-event={ Meetup.Boston.dayone_event_id } class="day clearfix">
      <div class="contained">
        <div id="talk-submissions">
          <div class="l">
            <h1>Day 01</h1>
            <h2>3.09.12</h2>
            <h3>
              <span>9am @<a href="http://www.meetup.com/nescala/events/37637442/">NERD</a></span>
            </h3>
            <p>Scala Talks</p>{
              rsvps
            }
          </div>
          <div class="r">
            <h1>Submit a Talk</h1>
            <p>This year's symposium features 16 talks of 30 minutes, one keynote talk, and one 45 - 60 minute panel discussion.</p>
            <p>Hopeful speakers and panelists may propose talks or panels on topics of their choosing. The schedule will be filled by talks that accrue the most votes, with the keynote spot (and $1000 travel offset) going to whichever receives the most votes of all.</p>
            <p>The voting polls open <strong>1/10</strong> and close <strong>1/24</strong>, but feel free to <a href="/2012/talks">peruse the current proposals</a>.</p>
            <p>What would make this year even more awesome than last year is hearing even more people talk. If you have an awesome project you want to get out in the open, solved a hard problem, made a brilliant discovery, or just have fun programming an Scala and want to talk about it, post a talk proposal below.</p>
          </div>
            {
              if(authed) {
                <div class="l divy">
                  <p class="instruct">
                    Please provide a single-paragraph description of your proposed talk.
                  </p>
                  <p class="instruct">
                    Speakers may enter Twitter usernames and other biographical
                    information in their
                    <a target="_blank" href="http://www.meetup.com/nescala/profile/">
                      member profile
                    </a>.
                  </p>
                  <p class="instruct">
                    Each person can post at most <strong>three</strong> talk proposals.
                  </p>
                </div>
              } else <div class="l divy"/>
            }
            <div class="r divy" id="propose-talk">
            {
              if(authed) {
                <div id="propose-container">
                  <form action="/boston/proposals" method="POST" id="propose-form"
                    class="proposing"
                    style={ "display:%s" format(
                      if(proposals.size < Proposals.MaxProposals) "visible" else "none"
                    )}>
                    <h4>Propose a talk</h4>
                    <div>
                      <label for="name">What's your talk called?</label>
                      <input type="text" name="name" maxlength={ Proposals.MaxTalkName + "" }
                        placeholder="How I learned to love my type system" />
                    </div>
                    <div>
                      <label for="desc">What's your talk is about?</label>
                      <div class="limited">
                        <textarea name="desc" data-limit={ Proposals.MaxTalkDesc + "" }
                          placeholder={ "Say it in %s characters or less" format(
                            Proposals.MaxTalkDesc) } />
                        <div>
                          <div class="limit-label"/>
                          <input type="submit" value="Propose Talk" class="btn" />
                        </div>
                      </div>
                    </div>
                  </form>
                  { proposalList(proposals) } 
                </div>
              } else {
                <span>
                  <a href="/connect?then=propose-talk">Log in with Meetup</a>
                  to submit a talk.
                </span>
              }
            }
          </div>
          {
            if(authed) {
              <div class="l divy">
                <p class="instruct">
                  One panel discussion will be chosen to be presented by a
                  group of peers after Friday's talks.
                </p>
                <p class="instruct">
                  Please provide a brief topic and description of what
                  you're panel would be about.
                </p>
                <p class="instruct">
                  One person can post at most <strong>three</strong>
                  proposals for panel topics.
                </p>
              </div>
            } else <div class="l divy"/>
          }
          <div class="r divy" id="propose-panel">
            {
              if(authed) {
                <div id="propose-panel-container">
                  <form action="/boston/panel_proposals" method="POST"
                    id="propose-panel-form" class="proposing"
                    style={ "display:%s" format(
                      if(panels.size < Panels.MaxProposals) "visible" else "none"
                    )}>
                    <h4>Propose a Panel</h4>
                    <div>
                      <label for="name">What's the topic of your panel?</label>
                      <input type="text" name="name"
                        maxlength={ Panels.MaxTalkName + ""}
                        placeholder="The sound of one million actors clapping" />
                    </div>
                    <div>
                      <label for="desc">What's your panel is about?</label>
                      <div class="limited">
                        <textarea name="desc" data-limit={ Panels.MaxTalkDesc + "" }
                            placeholder="The state of async Scala" />
                        <div>
                          <div class="limit-label"/>
                          <input type="submit" value="Propose Panel" class="btn" />
                        </div>
                      </div>
                    </div>
                  </form>
                  { panelList(panels) } 
                </div>
              } else {
                <span>
                  <a href="/connect?then=propose-panel">Log in with Meetup</a>
                  to submit a proposal.
                </span>
              }
            }
          </div>
        </div>
      </div>
    </div>

  private val dayTwo =
    <div id="day-two" data-event={ Meetup.Boston.daytwo_event_id } class="day clearfix">
      <div class="contained">
        <div class="l">
          <h1>Day 02</h1>
          <h2>3.10.12</h2>
          <h3>
            <span>10am @<a href="http://www.meetup.com/nescala/events/44042982/">Stata Center</a></span>
          </h3>
          <p>Scala Workshops</p>
          { rsvps }
        </div>
        <div class="r">
          <p>
            The second day of the symposium is hands-on Scala hacking and workshops
            hosted at MIT.
          </p>
        </div>
      </div>
    </div>

  private val dayThree =
    <div id="day-three" data-event={ Meetup.Boston.daythree_event_id }
      class="day clearfix">
      <div class="contained">
        <div class="l">
          <h1>Day 03</h1>
          <h2>3.11.12</h2>
          <h3>
            <span>10am @<a href="http://www.meetup.com/nescala/events/44049692/">Stata Center</a></span>
          </h3>
          { rsvps }
        </div>
        <div class="r">No details yet</div>
      </div>
    </div>

  private def index(
    authed: Boolean,
    proposals: Seq[Map[String, String]] = Nil,
    panels: Seq[Map[String, String]] = Nil) =
    bostonLayout(Nil)(
    <script type="text/javascript" src="/js/jquery.scrollTo-1.4.2-min.js"></script>
    <script type="text/javascript" src="/js/boston.js"></script>)(
      head ++ dayOne(authed, proposals, panels) ++ dayTwo ++ dayThree
    )
}

object Templates extends Templates {}
