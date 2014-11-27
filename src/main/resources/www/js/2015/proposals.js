jQuery(document).ready(function () {
    // fancy pants character counting
    $(document).on('focus', '.limited textarea', function() {
      var self = $(this);
      $(this).animate({"height":"9em"}, function() {
          //$.scrollTo('#'+self.parent().parent().parent().parent().parent().attr("id"),500);
      });
    });
    $(document).on('keyup', '.limited textarea', function() {
      var ta = $(this)
        , l = parseInt(ta.data().limit,10)
        , v = ta.val()
        , n = v.length;
      if (n > l) {
        ta.val(v.substring(0, l));
        n = l;
      }
      ta.parent().parent().find(".limit-label").text(
        l - n + " characters remaining"
      );
    });

  // works for both create & edit
  $(".propose").on('click', function(event) {
    event.stopPropagation();
    var data = $(this).data(), modal = $(".modal");
    var redact = function(id) { 
      return '<div class="unit whole center-on-mobiles note">\
         <p>If you would like to withdrawl this proposal for any reason,\
         feel free to reach out and <a href="mailto:doug@meetup.com?subject=please withdrawl talk {id}">let us know</a><i class="fa fa-paper-plane"></i>.</p>\
       </div>'.replace(/{id}/g, id);
    }
    var form =
     '<form action="/2015/talks/{id}" method="post">\
       <div class="grid">\
         <div class="unit half right center-on-mobiles">\
          <label for "kind">Talk length</label>\
         </div>\
         <div class="unit half left center-on-mobiles">\
            <select name="kind">\
              <option value="medium">Medium (45 min)</option>\
              <option value="short">Short (30 min)</option>\
              <option value="lightning">Lightning (15 min)</option>\
            </select>\
         </div>\
        </div>\
        <div class="grid">\
          <div class="unit whole">\
            <label for="name">Give your talk a good name (short and sweet)</label>\
          </div>\
          <div class="unit whole">\
            <input type="text" name="name" value="{name}" maxlength="200"\
              placeholder="How I learned to love my typesystem"/>\
          </div>\
        </div>\
       <div class="grid">\
         <div class="unit whole">\
          <label for="desc">Give us a reason to listen. <a href="/2014/talks" target="_blank">Here</a> are some good examples.</label>\
         </div>\
         <div class="limited unit whole">\
          <textarea placeholder="Say it in 600 characters or less"\
            name="desc" data-limit="600">{desc}</textarea>\
           <div class="limit-label">{desclen} characters remaining</div>\
         </div>\
        </div>\
        <div class="grid">\
          <div class="unit half right less-top-padding center-on-mobiles">\
            <input type="submit" class="btn" id="proposal-edit" value="Put it out there"/>\
           </div>\
           <div class="unit half left center-on-mobiles">\
             <a href="#" class="btn cancel" id="proposal-edit-cancel">Maybe later</a>\
           </div>\
           {redact}\
         </div>\
      </form>'.replace(/{id}/g, data.id || "")
              .replace(/{name}/g, data.name || "")
              .replace(/{desc}/g, data.desc || "")
              .replace(/{desclen}/g, data.desc ? 600 - data.desc.length : 600)
              .replace(/{redact}/g, data.id ? redact(data.id) : "");
    modal.find('.form').html(form);
    modal.find('.form select').val(data.kind || "short");
    modal.addClass("active");
    return false;
  });

  $(".modal").on('submit', 'form', function(event) {    
    var form = $(this);
    if (!form.hasClass('validated')) {
      event.preventDefault(); 
      var data = form.serializeArray(), valid = true;
      $.each(data, function() {
        var len = this.value.length;
        if ('name' == this.name) {
          if (len < 1) {
            alert("talk name is required");
            valid = false;
            return valid;
          }
          if (len > 200) {
            alert('talk name should be at most 200 characters');
            valid = false;
            return valid;
          }
        }
        if ('desc' == this.name) {
          if (len < 1) {
            alert("talk description is required");
            valid = false;
            return valid;
          }
          if (len > 600) {
            alert('talk description should be at most 600 characters');
            valid = false;
            return valid;
          }
        }
      });
      if (valid) {
        form.addClass('validated');
        return form.submit();
      } else {
        return false;
      }
    }    
  });

  $(".modal").on('click', '.cancel', function(e) {
    e.preventDefault();
    $(".modal").removeClass("active");
    return false;
  });
});
