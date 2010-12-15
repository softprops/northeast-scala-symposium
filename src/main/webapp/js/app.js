(function($){
  $.ajaxSetup({
    'beforeSend': function(xhr) {
        xhr.setRequestHeader("Accept", "text/javascript");
     }
  });

  $.getJSON("/rsvps?callback=?", function(typers) {
      var s = typers.length, ul = $("#rsvps");
      $("#tban").html(s + " functional types");
      for(var tp in typers) {
          var t = typers[tp];
          ul.append('<li><a href="http://www.meetup.com/ny-scala/members/'+t.id+'"><span><img src="'+t.photo+'" title="'+t.name+'"/></span></a></li>');
      }
      $('#rsvps li img').tipsy({fade:true, live:true, gravity:'sw'});
  });

  $.getJSON("/event?callback=?", function(events) {
      var open = parseInt(events[0].limit) - parseInt(events[0].yes);
      $("#seats").html(open + " open seats");
  });
})(jQuery);