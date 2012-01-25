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
        $(".tban", day).html("<strong>"+s+"</strong> functional types");
        var n = typers.length, randomtypes = fyates(typers).splice(0, 40), buffer=[];
        for(t in randomtypes) {
          buffer.push(template(randomtypes[t]));
        }
        ul.append(buffer.join(''));
        ext.html('with ' + (n - 40) + ' others');
        $('.rsvps li img', day).tipsy({fade:true, live:true, gravity:'sw'});
      });
    });

  });
})(jQuery);