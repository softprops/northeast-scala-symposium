(function($){
  $(function() {
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

    $("#propose-form form").submit(function(e){
      e.preventDefault();
      var frm = $(this), fields = $("input,textarea", frm), data = frm.serialize();
      fields.attr('disabled', 'disabled');
      $.post("/boston/proposals", data, function(e) {
        fields.removeAttr('disabled');
        switch(e.status) {
        case 200:
          $("#proposal-list").append(
            '<li id="'+e.id+'">'+$("input[name='name']",frm).val()+'</li>'
          );
          $('input[type="text"],textarea').val('');
          switch(e.proposals) {
          case 1:
            alert("Sweet! Now you have a proposal.");
            break;
          default:
            alert('Sweet! Now you now have ' + e.proposals + ' talks');
            break;
          }
          break;
        case 400:
          alert(e.msg);
          break;
        }
      });
      return false;
    });
  });
})(jQuery);