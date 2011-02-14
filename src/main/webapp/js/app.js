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
      if(open > 0) {
        $("#seats").html(open + " open seats");
      }
  });

})(jQuery);