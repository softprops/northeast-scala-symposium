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
          ta.text(v.substring(0, n));
          n = l;
        }
        ta.parent().find(".limit-label").text(
           l - n + " remaining"
        );
    });

    $("#propose-form form").submit(function(e){
      e.preventDefault();
      $(this).find("input[type='submit']").attr('disabled', 'disabled');
      console.log('submit');
      return false;
    });
  });
})(jQuery);