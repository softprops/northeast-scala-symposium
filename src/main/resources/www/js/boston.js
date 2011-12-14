(function($){
  $.ajaxSetup({
    'beforeSend': function(xhr) {
      xhr.setRequestHeader("Accept", "text/javascript");
    }
  });
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
    $("div.day").each(function(){
      var day = $(this), event = day.data().event;
      $.get("/boston/rsvps/" + event, function(typers) {
        var s = typers.length, ul = $(".rsvps", day)
        , ext = $(".extra-rsvps", day),
        template = function(t) {
          return ['<li><a href="http://www.meetup.com/nescala/members/'
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

    $("div.limited textarea").focus(function(){
      $(this).animate({"height":"9em"});
    }).keyup(function() {
        var ta = $(this)
        , l = parseInt(ta.data().limit,10)
        , v = ta.val()
        , n = v.length;
        if(n > l) {
          ta.val(v.substring(0, l));
          n = l;
        }
        ta.parent().find(".limit-label").text(
           l - n + " characters remaining"
        );
    });

    $("#proposal-list .preview").hide();
    $("#proposal-list .toggle").live('click', function(e){
      e.preventDefault();
      $(this).parent().parent().find(".preview").slideToggle(100);
      return false;
    });
    $("#proposal-list .withdraw-proposal").live('click', function(e) {
      e.preventDefault();
      var self = $(this), pid = self.data().proposal, href = self.attr('href');
      if(confirm("Are you sure you want to withdraw this talk?")) {        
        $.post(href, { id:pid }, function(e){
          switch(e.status) {
          case 200:
            self.parent().parent().parent().fadeOut('fast', function(){
             $(this).remove();
              $('#propose-form').fadeIn('slow');
            });
            break;
          case 400:
            alert(e.msg);
            break;
          }
        });
      }
      return false;
    });

    $("#propose-form").submit(function(e){
      e.preventDefault();
      var frm = $(this)
        , fields = $("input,textarea", frm)
        , data = frm.serialize()
        , name = $('input[name="name"]', frm)
        , desc = $('textarea', frm);
      if(!name.val().length || !desc.val().length) { alert('Please enter a name and talk description'); }
      else {
      fields.attr('disabled', 'disabled');
      $.post("/boston/proposals", data, function(e) {
        fields.removeAttr('disabled');
        switch(e.status) {
        case 200:
          $("#proposal-list").append(
             '<li id="'+e.id+'"><div><a href="#" class="toggle">'+$("input[name='name']",frm).val()+'</a> <div class="controls"><a href="/boston/proposals/withdraw" class="withdraw-proposal" data-proposal="'+e.id+'">withdraw</a></div></div><div class="preview linkify">'+$('textarea', frm).val()+'</div></li>'
          );
          $('input[type="text"],textarea').val('');
          switch(e.proposals) {
          case 1:
            alert("Sweet! Now you have a proposal.");
            break;
          default:
            alert('Sweet! Now you now have ' + e.proposals + ' proposals');
            if(e.proposals === 3) {
              $('#propose-form').fadeOut('slow');
            } else {
              $('#propose-form').fadeIn('slow');
            }
            break;
          }
          break;
        case 400:
          alert(e.msg);
          break;
        }
      });
      }
      return false;
    });

    !function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="http://platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");
  });
})(jQuery);