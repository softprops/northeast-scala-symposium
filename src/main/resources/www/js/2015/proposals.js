jQuery(document).ready(function () {
    // fancy pants character counting
    $("div.limited").on('focus', 'textarea', function(){
      var self = $(this);
      $(this).animate({"height":"9em"}, function(){
          //$.scrollTo('#'+self.parent().parent().parent().parent().parent().attr("id"),500);
      });
    });
    $('div.limited').on('keyup', 'textarea', function() {
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
});
