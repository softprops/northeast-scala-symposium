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
      $.get("/philly/rsvps/" + event, function(typers) {
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
        $(".tban", day).html("a sampling of the functional types attending");
        var n = typers.length
          , partioned = photoPartioned(typers)
          , randomtypes = fyates(partioned[0]).concat(partioned[1]).splice(0, 120)
          , buffer = [];
        for(t in randomtypes) {
          buffer.push(template(randomtypes[t]));
        }
        ul.append(buffer.join(''));
        $('.rsvps li img', day).tipsy({fade:true, live:true, gravity:'sw'});
      });
    });

    // fancy pants character counting
    $("div.limited textarea").live('focus', function(){
      var self = $(this);
      $(this).animate({"height":"9em"}, function(){
          $.scrollTo('#'+self.parent().parent().parent().parent().attr("id"),500);
      });
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

    $("div.preview").hide();
    $("a.toggle").live('click', function(e){
      e.preventDefault();
      $(this).parent().parent().find(".preview").slideToggle(100);
      return false;
    });

    var notify = function(msg, err, selector) {
      $(selector || "#proposal-notification")
         .empty()
         .hide()
         .html(msg)
         .removeClass("ok err")
         .addClass(err ? "err" : "ok")
         .fadeIn(200, function(){
            var self = $(this)
            , hide = function() {
                self.fadeOut("slow");
            };
            setTimeout(hide, 4000)
        });
    };

    // for wuses
    $("a.withdraw").live('click',
      function(e) {
      e.preventDefault();
      var self = $(this), href = self.attr('href');
      if(confirm("Are you sure you want to withdraw this?")) {        
          $.post(href, {}, function(e){
          switch(e.status) {
          case 200:
            // surely there is a better way to peek up the family chain
            self.parent().parent().parent().parent()
              .parent().parent().fadeOut('fast', function(){
              $(this).remove();
              $(self.data().sourceform).fadeIn('slow');
            });
            break;
          case 400:
            notify(e.msg, true);
            break;
          }
        });
      }
      return false;
    });

    // editing
    $("form.propose-edit-form").live('submit', function(e){
      e.preventDefault();
      var self = $(this)
        , action = self.attr('action')
        , controls = $('.controls', self)
        , preview = $('.preview', self)
        , name = $.trim($('input[name="name"]', self).val())
        , desc = $.trim($('textarea[name="desc"]', self).val())
        , data = self.serialize()
        , fields = $('input, textarea', self);

      if(!name.length || !desc.length) {
          notify('<strong>Name</strong> and <strong>talk</strong> description are required',
                 true,
                "#proposal-edit-notification");
      } else if(name.length > 200 || desc.length > 600) {
          notify("Contents are too long",
                 true,
                "#proposal-edit-notification");
      } else {
        fields.attr('disabled', 'disabled');
        $.post(action, data, function(r) {
          fields.removeAttr('disabled');
          switch(r.status) {
          case 200:
              $('input[name="name"], .edit-desc', self).hide();
              $('.name', self).html(name).data().name = name;
              $('.desc', self).html(desc).data().desc = desc;
              $('.name, .desc', self).show();
              break;
          case 400:
              notify(r.msg, true, "#proposal-edit-notification");
              break;
          };
        });
      }
      return false;
    });

    $("a.edit-proposal").live('click', function(e){
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

    // out of commision
    $("#propose-form").submit(function(e){
      e.preventDefault();
      var frm = $(this)
        , isPanel =  frm.attr("id") === 'propose-panel-form'
        , lid = isPanel ? '#panel_proposals-list' : '#proposals-list' 
        , action = frm.attr("action")
        , fields = $("input,textarea", frm)
        , data = frm.serialize()
        , name = $('input[name="name"]', frm)
        , desc = $('textarea', frm);
      if(!name.val().length || !desc.val().length) {
        notify('Please enter both a <strong>name</strong> and <strong>description</strong> for your talk',
               true);
      } else {
        var descTxt = desc.val(), nameTxt = name.val();
        fields.attr('disabled', 'disabled');
        $.post(action, data, function(e) {
          fields.removeAttr('disabled');
          switch(e.status) {
          case 200:
            $("#no-proposals").remove()
            $(lid).append(
              ['<li id="', e.id,'">'
               ,'<form action="',action,'/',encodeURIComponent(e.id)
               ,'" method="POST" class="propose-edit-form">'
               , '<div>'
               , '<a href="#" class="toggle name" data-val="', nameTxt,'">'
               , nameTxt, '</a>'
               ,'<input type="text" name="name" maxlength="200" value="', nameTxt,'" />'
               ,'</div>'
               ,'<div class="preview">'
               , '<div class="controls clearfix">'
               , '<ul>'
               , '<li><a href="mailto:doug@meetup.com?subject=please withdraw talk ', e.id,'">Email us</a> if you wish to withdraw this.</li>'
               , '<li><a href="#" class="edit-proposal" data-proposal="',e.id,'">edit</a></li>'
               , '</ul>'
               , '</div>'
               , '<div class="linkify desc" data-val="',descTxt,'">',descTxt,'</div>'
               , '<div class="edit-desc limited">'
               ,   '<textarea data-limit="600" name="desc">',descTxt,'</textarea>'
               ,   '<div class="form-extras">'
               ,     '<div class="limit-label"/>'
               ,     '<div class="edit-controls clearfix">'
               ,       '<input type="submit" value="', (isPanel ? 'Edit Panel' : 'Edit Talk'),'" class="btn" />'
               ,       '<input type="button" value="Cancel" class="btn cancel" />'
               ,     '</div>'
               ,   '</div>'
               , '</div>'
               ,'</div>'
               ,'</form>'
               ,'</li>'].join('')
            );
            name.val(''); desc.val('');
            $(lid +" .preview").hide();
            switch(e.proposals) {
            case 1:
              notify("Sweet! Now you have a proposal. You may find and edit it under your <a href='#proposals'>talk listing</a>.",
                     false);
              break;
            default:
              notify('Sweet! Now you now have ' +
                     e.proposals +
                     ' proposals. You may find and edit it under your <a href="#proposals">talk listing</a>',
                     false);
              if(e.proposals === 3) {
                $(frm).fadeOut('slow');
              } else {
                $(frm).fadeIn('slow');
              }
              break;
            }
            break;
          case 400:
            notify(e.msg, true);
            break;
          }
        });
      }
      return false;
    });


  });
})(jQuery);
