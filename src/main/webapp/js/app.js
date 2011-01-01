(function($){
  $.ajaxSetup({
    'beforeSend': function(xhr) {
        xhr.setRequestHeader("Accept", "text/javascript");
     }
  });

  $.getJSON("/rsvps?callback=?", function(typers) {
      var s = typers.length, ul = $("#rsvps"),
      template = function(t) {
          return ['<li><a href="http://www.meetup.com/ny-scala/members/', t.id, '"><span><img src="', t.photo,'" title="',t.name,'"/></span></a></li>'].join('');
      };
      $("#tban").html(s + " functional types");
      for(t in typers) {
          ul.append(template(typers[t]));
      }
      $('#rsvps li img').tipsy({fade:true, live:true, gravity:'sw'});
  });

  $.getJSON("/event?callback=?", function(events) {
      var open = parseInt(events[0].limit) - parseInt(events[0].yes);
      $("#seats").html(open + " open seats");
  });

  $.getJSON("/twttr?callback=?", function(tweets) {
      var s = tweets.length, ul = $("#tweets"), linkify = function(raw) {
        var links = /((ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?)/gi,
          users = /[\@]+([A-Za-z0-9-_]+)/gi,
          hashes = /(?:^| )[\#]+([A-Za-z0-9-_]+)/gi;
        return raw.replace(links, "<a href=\"$1\">$1</a>").
          replace(users, "<a href=\"http://twitter.com/$1\">@$1</a>").
          replace(hashes, ' <a href="http://search.twitter.com/search?q=&tag=$1&lang=all">#$1</a>');
      }, relativize = function(time) {
        var delta = parseInt((new Date().getTime() - new Date(time)) / 1000), r = '';
        if (delta < 60) { r = delta + ' seconds ago'; }
        else if(delta < 120) { r = 'a minute ago'; }
        else if(delta < (45*60)) { r = (parseInt(delta / 60, 10)).toString() + ' minutes ago'; }
        else if(delta < (2*60*60)) { r = 'an hour ago'; }
        else if(delta < (24*60*60)) { r = '' + (parseInt(delta / 3600, 10)).toString() + ' hours ago'; }
        else if(delta < (48*60*60)) { r = 'a day ago'; }
        else { r = (parseInt(delta / 86400, 10)).toString() + ' days ago'; }
        return 'about ' + r;
      }, template = function(t) {
        return ['<li><q>',linkify(t.text),'</q>',
          '<cite> &ndash; <a href="http://twitter.com/',t.user,'">@',t.user,'</a> <a href="http://twitter.com/',t.user,'/status/', t.id, '">', relativize(t.created_at), '</a></cite></li>'].join('');
      };
      if(s > 0) {
        $("#tweets-header").text(s + " recent bird " + (s == 1 ? "call" : "calls"));
      }
      for(t in tweets) {
        ul.append(template(tweets[t]));
      }
  });

})(jQuery);