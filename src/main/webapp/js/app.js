(function($){
  $.getJSON("/rsvps?callback=?", function(typers) {
      var s = typers.length, ul = $("#rsvps");
      $("#tban").html(s + " functional types");
      for(var tp in typers) {
          var t = typers[tp];
          ul.append('<li><a href="http://www.meetup.com/ny-scala/members/'+t.id+'"><span><img src="'+t.photo+'" title="'+t.name+'"/></span></a></li>');
      }
      $('#rsvps li img').tipsy({fade:true, live:true, gravity:'sw'});
  });
  var pass = function() {
    var i  = $('<img class="i" src="/images/c.png" />'), ref = $(i);
    $("#container").append(i);
    i.css({"top": (Math.random()*$(window).height()) + "px", "left": "-300px" });
    i.animate({"left": $(window).width() + "px"}, 30000, 'swing', function() {
      $(this).remove();
    });
  };
  setInterval(pass, 15000);
  setTimeout(pass, 500);
})(jQuery);