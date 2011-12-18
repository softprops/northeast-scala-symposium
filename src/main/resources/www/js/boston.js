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

    // fancy pants character counting
    $("div.limited textarea").live('focus', function(){
      $(this).animate({"height":"9em"});
    });
    $('div.limited textarea').live('keyup', function() {
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

    // for wuses
    $("#proposal-list .withdraw-proposal").live('click', function(e) {
      e.preventDefault();
      var self = $(this), pid = self.data().proposal, href = self.attr('href');
      if(confirm("Are you sure you want to withdraw this talk?")) {        
        $.post(href, { id:pid }, function(e){
          switch(e.status) {
          case 200:
            self.parent().parent().parent().parent().parent().parent().fadeOut('fast', function(){
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

    $("form.propose-edit-form").live('submit', function(e){
      e.preventDefault();
      var self = $(this)
        , controls = $('.controls', self)
        , preview = $('.preview', self)
        , name = $.trim($('input[name="name"]', self).val())
        , desc = $.trim($('textarea[name="desc"]', self).val())
        , data = self.serialize()
        , fields = $('input, textarea', self);

      if(!name.length || !desc.length) {
          alert('Name and description are required for talk proposals');
      } else if(name.length > 200 || desc.length > 600) {
          alert("Talk contents are too long");
      } else {
          fields.attr('disabled', 'disabled');
        $.post('/boston/proposals/' + encodeURIComponent($('input[name="id"]', self).val()), data, function(r) {
          fields.removeAttr('disabled');
          switch(r.status) {
          case 200:
              $('input[name="name"], .edit-desc', self).hide();
              $('.name, .desc', self).show();
              break;
          case 400:
              alert(r.msg);
              break;
          };
        });
      }
      return false;
    });

    $("form.propose-edit-form .edit-proposal").live('click', function(e){
      e.preventDefault();
      var self = $(this)
        , controls = self.parent().parent().parent()
        , preview = controls.parent()
        , frm = preview.parent()
        , name = $('.name', frm)
        , orgName = name.data().val
        , nameIn = $('input[name="name"]', frm)
        , desc = $('.desc', preview)
        , orgDesc = desc.data().val
        , editDesc = $('.edit-desc', frm)
        , descIn = $('textarea[name="desc"]', frm)
        , cancel = $('.cancel', frm);

      name.hide();
      desc.hide();
      editDesc.show();
      nameIn.show();
      descIn.show();

      cancel.click(function(e) {
        e.preventDefault();
        name.show();
        desc.show();
        editDesc.hide()
        nameIn.val(orgName).hide();
        descIn.val(orgDesc).hide();
        return false;
      });
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
        var descTxt = desc.val(), nameTxt = name.val();
        fields.attr('disabled', 'disabled');
        $.post("/boston/proposals", data, function(e) {
          fields.removeAttr('disabled');
          switch(e.status) {
          case 200:
            $("#proposal-list").append(
              ['<li id="', e.id,'">'
               ,'<form action="#" method="POST" class="propose-edit-form">'
               , '<input type="hidden" name="id" value="', e.id,'"/>',
               , '<div>'
               , '<a href="#" class="toggle name" data-val="', nameTxt,'">', nameTxt, '</a>'
               ,'<input type="text" name="name" maxlength="200" value="', nameTxt,'" />'
               ,'</div>'
               ,'<div class="preview">'
               , '<div class="controls clearfix">'
               , '<ul>'
               , '<li>'
               ,    '<a href="/boston/proposals/withdraw" class="withdraw-proposal" data-proposal="',e.id,'">withdraw</a>'
               , '</li>'
               , '<li><a href="#" class="edit-proposal" data-proposal="',e.id,'">edit</a></li>'
               , '</ul>'
               , '</div>'
               , '<div class="linkify desc" data-val="',descTxt,'">',descTxt,'</div>'
               , '<div class="edit-desc limited">'
               ,   '<textarea data-limit="600" name="desc">',descTxt,'</textarea>'
               ,   '<div class="form-extras">'
               ,     '<div class="limit-label"/>'
               ,     '<div class="edit-controls clearfix">'
               ,       '<input type="submit" value="Edit Talk" class="btn" />'
               ,       '<input type="button" value="Cancel" class="btn cancel" />'
               ,     '</div>'
               ,   '</div>'
               , '</div>'
               ,'</div>'
               ,'</form>'
               ,'</li>'].join('')
            );
            name.val(''); desc.val('');
            $("#proposal-list .preview").hide();
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