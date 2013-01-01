package nescala.philly

import nescala.Meetup

trait Templates extends nescala.Templates with SponsorTemplate {
  import java.net.URLEncoder.encode

  val meetupGroup = "http://www.meetup.com/nescala/"
  val eventLink = meetupGroup + "/events/" + Meetup.Philly.eventId + "/"

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

  def login(authed: Boolean, then: String = "") = <span class="none"/>
    /*if(!authed) <div id="auth-bar" class="clearfix"><div class="contained"><div class="l">Just who are you anyway?</div><div class="r"><a href={ "/login%s" format(if(then.isEmpty) "" else "?then=%s".format(then)) } class="btn login">Log in with Meetup</a></div></div></div> else <span/>*/

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
        <h4>in Philly</h4>
        <h4>Feb 8-9, 2013</h4>
        <hr/>
      </div>
    </div>
   </div> ++ { login(authed, afterlogin) }

  // listing of talk proposals (refactor plz)
  def talkListing(
    proposals: Seq[Map[String, String]],
    canVote: Boolean = false,
    votes: Seq[String] = Seq.empty[String]) = phillyLayout(
    <script type="text/javascript" src="/js/philly/voting.js"></script>)(Nil)({
      head(canVote, "vote")
    } ++ <div class="contained">
     <div id="maybe-talks-header">
        <h2 id="proposed">{ proposals.size } Scala campfire stories</h2>
        <div>This year's symposium features talks from members of the Scala community. Below is a list of current talk proposals.</div>{ if (canVote) <div id="votes-remaining">You have { Votes.MaxTalkVotes - votes.size match {
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
                } else <span/> }
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
      <h4 class="tban"/><ul class="rsvps"/><p class="extra-rsvps"/>
    </div>

  val drexelMap = "http://goo.gl/maps/s0n7h"
  
  val blurb =
    <div id="blurb">
      <div class="contained">
      <p>Just <a href={ eventLink }>RSVP</a> <span class="amp">&amp;</span> you're in.</p>
      <hr/>
      <p>
        As in years <a href="/2012">past</a>, this symposium features talks by you, the attendees,
        so we need you to <a href="#speakup">post your ideas</a> for talks.
      </p>
      <p>
        Talks are also choosen by you. Please browse the <a href="/2013/talks#proposed">current talk list</a> and vote for the talks you want to see.
      </p>
      { twttrFollow }
      </div>
    </div>

  def dayOne(authed: Boolean,
             keynote: Map[String, String],
             talks: Seq[Map[String, String]],
             proposals: Seq[Map[String, String]]): xml.NodeSeq = {
   <div id="day-one" data-event={ Meetup.Philly.eventId } class="day clearfix">
      <div class="contained">
        <div id="speakup">
          <div class="l">
            <h1>Talking</h1>
          </div>
          <div class="r">
            <p>This year's symposium features <span>{Proposals.TalkTime}</span> minute talks.</p>
            <p>Hopeful speakers may propose talks on topics of their choosing. The schedule will be filled by talks that accrue the most votes, with the keynote spot going to whichever proposal receives the most votes.</p>
          </div>
          <div class="l"/><hr/>{
          if (!authed) {
            <div class="r">
              <h2>Speak up</h2>
              <p>In order to submit a talk proposals, we need to know you are attending.</p>
              <p><a href="/login?then=talk" class="btn">Login to talk</a></p>
            </div>
          } else propose(proposals)
        }
      </div>
      <div id="voting">
        <div class="l divy">
          <h1>Voting</h1>
        </div>
        <div class="r divy">
          { if (!authed) {
            <p>Talks are selected by you, but we need to know you are attending first.</p>
            <p><a href="/login?then=vote" class="btn">Login to vote</a></p>
          } else { 
            <p>Talks are selected by you, so <a href="/2013/talks">get voting</a> now!</p>
          }
         }
        </div>
      </div>
    </div>
  </div>
  }

  def propose(proposals: Seq[Map[String, String]]): xml.NodeSeq = {
    <div class="l" id="proposal-notifications">
      <div id="proposal-notification"/>
      <div id="proposal-edit-notification"/>
    </div>
    <div class="r" id="propose-talk">
      <h2>Speak up</h2>
      <p class="instruct">
        Please provide a brief single-paragraph description of your proposed talk.
      </p>
      <p class="instruct">
        Speakers may enter Twitter usernames and other biographical
        information in their
        <a target="_blank" href="http://www.meetup.com/nescala/profile/">
          Meetup member profile
        </a>.
      </p>
      <p class="instruct">
        Each attendee can post at most <strong>three</strong> talk proposals.
      </p>
      <div id="propose-container" class="divy">
        <form action="/philly/proposals" method="POST" id="propose-form"
          class="proposing" style={ "display:%s" format(
              if (proposals.size < Proposals.MaxProposals) "visible" else "none"
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
  }


  def indexPage(
    authed: Boolean,
    keynote: Map[String, String] = Map.empty[String,String],
    talks: Seq[Map[String, String]] = Nil,
    proposals: Seq[Map[String, String]] = Nil) =
    phillyLayout(Nil)(
    <script type="text/javascript" src="/js/jquery.scrollTo-1.4.2-min.js"></script>
    <script type="text/javascript" src="/js/philly/index.js"></script>)(
      head(authed) ++ blurb ++ dayOne(authed, keynote, talks, proposals)
    )
}

object Templates extends Templates {}
