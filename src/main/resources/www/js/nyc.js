window.nyc = (function($){
  $.ajaxSetup({
    'beforeSend': function(xhr) {
        xhr.setRequestHeader("Accept", "text/javascript");
     }
  });

  $("#talkers dd .t").hide();
  $("#talkers dd").hover(
    function(){ $(this).find(".t").fadeIn("fast"); },
    function(){ $(this).find(".t").fadeOut("fast"); }
  );

  $("#pban").hide();

  $.getJSON("/nyc/rsvps?callback=?", function(typers) {
      var s = typers.length, ul = $("#rsvps"),
      template = function(t) {
          return ['<li><a href="http://www.meetup.com/ny-scala/members/'
                  , t.id
                  , '"><span><img src="'
                  , t.photo
                  , '" title="'
                  , t.name
                  ,'"/></span></a></li>'].join('');
      };
      $("#tban").html("<strong>"+s+"</strong> functional types");
      for(t in typers) {
          ul.append(template(typers[t]));
      }
      $('#rsvps li img').tipsy({fade:true, live:true, gravity:'sw'});
  });

 $.getJSON("/nyc/photos?callback=?", function(photos) {
   var ul = $("#photos"), template = function(p) {
       return ['<li><a target="_blank" href="'
               , p.photo_link
               ,'" rel="facebox"><img src="'
               , p.thumb_link
               ,'"/></a></li>'].join('');
   };
   for(p in photos) {
     ul.append(template(photos[p]));
   }
   $("#pban").show();
   $('a[rel*=facebox]').facebox();
  });

})(jQuery);