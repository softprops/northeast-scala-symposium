(function($){
  $(function(){
    $("body").on('submit',".ballot", function(e) {
      e.preventDefault();
      var self = $(this)
        , action = self.attr("action")
        , method = self.attr("method");
      $('.btn', self).attr('disabled', 'disabled');
      $.ajax({
        'url': action,
        'type': method,
        'data': self.serialize(),
        'success': function(r){
          switch(r.status) {
            case 200:
              var btn = $('.btn', self);
              btn.toggleClass('voted-yes');
              self.attr("method", "POST" === method ? "DELETE" : "POST");
              btn.text(btn.hasClass('voted-yes') ? ' Change your mind?': " Let's make this happen").removeAttr('disabled');
              switch(r.remaining) {
                case 0: $('#votes-remaining').html('You have <strong>no votes</strong> remaining'); break;
                case 1: $('#votes-remaining').html('You have <strong>one vote</strong> remaining'); break;
                default:$('#votes-remaining').html('You have <strong>' + r.remaining + ' votes</strong> remaining'); break;
              }         
              var unvoted = $('.ballot .btn:not(".voted-yes")');
              if (r.remaining <= 0) {
               unvoted.attr('disabled', 'disabled')
              } else {
                unvoted.removeAttr('disabled');
              }
              break;
            case 400:
              alert(r.msg);
              break;
          };
        }});
        return false;
      });
  });
})(jQuery);
