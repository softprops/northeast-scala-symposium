// index page
(function($){

  $(function() {

    var fyates = function(a) {
      var i = a.length, j, temp;
      if(!i) return;
      while(--i) {
        j = Math.floor(Math.random() * (i+1));
        temp = a[i]; a[i] = a[j]; a[j] = temp;
      }
      return a;
    }
    , photoPartioned = function(a) {
        var w = [], wo = [], def = 'http://img1.meetupstatic.com/39194172310009655/img/noPhoto_50.gif';
        for(var i = 0, l = a.length; i < l; i++) {
            var r = a[i];
            (r.photo === def ? wo : w).push(r);
        }
        return [w, wo];
    };

    // sprinkle on extra info about each day
    $("div.day").each(function(){
      var day = $(this), event = day.data().event;
      $.get("/boston/rsvps/" + event, function(typers) {
        var s = typers.length, ul = $(".rsvps", day)
        , ext = $(".extra-rsvps", day),
        template = function(t) {
          return ['<li><a target="_blank" href="http://www.meetup.com/nescala/members/'
                  , t.id
                  , '"><span><img src="'
                  , t.photo
                  , '" title="'
                  , t.name
                  ,'"/></span></a></li>'].join('');
        };
        $(".tban", day).html("functional types attended");
        var n = typers.length
          , partioned = photoPartioned(typers)
          , randomtypes = fyates(partioned[0]).concat(partioned[1]).splice(0, 45)
          , buffer = [];
        for(t in randomtypes) {
          buffer.push(template(randomtypes[t]));
        }
        ul.append(buffer.join(''));
        $('.rsvps li img', day).tipsy({fade:true, live:true, gravity:'sw'});
      });
    });

  });
})(jQuery);