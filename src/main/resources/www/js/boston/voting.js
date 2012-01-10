(function($){
  $(function(){
    $(".ballot").live('submit', function(e) {
      e.preventDefault();
        var self = $(this);
      $('.btn', self).attr('disabled', 'disabled');
      $.post('/boston/votes', self.serialize(), function(r){
        switch(r.status) {
        case 200:
          var btn = $('.btn', self);
          btn.toggleClass('voted-yes');
          btn.attr('value', btn.hasClass('voted-yes') ? 'Withdraw Vote': 'Vote').removeAttr('disabled');
          $('input[name="action"]', self).attr('value', btn.hasClass('voted-yes') ? 'unvote': 'vote');
          switch(r.remaining) {
          case 0: $('#votes-remaining').html('You have no votes remaining'); break;
          case 1:  $('#votes-remaining').html('You have one vote remaining'); break;
          default:  $('#votes-remaining').html('You have ' + r.remaining + ' votes remaining'); break;
          }         
          var unvoted = $('.ballot input[type="submit"]:not(".voted-yes")'); console.log(unvoted);
          if(r.remaining <= 0) {
             unvoted.attr('disabled', 'disabled')
          } else {
            unvoted.removeAttr('disabled');
          }
          break;
        case 400: alert(r.msg); break;
        };
      });
      return false;
    });
  });
})(jQuery);