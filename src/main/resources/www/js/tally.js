(function($){
  $(function() {
    var tallies = $("#tallies")
    , total = parseInt(tallies.attr("data-total"), 10);
    $(tallies).children().each(function(i,e) {
      var entry = $(e), votes = parseInt(entry.attr("data-score"));
      entry.click(function(){
          window.location.href = window.location.href.replace("_tally","s") + "#" + e.id.split(':').pop();
      });
      entry.find(".bar").animate({"width": votes + "%"}, 500);
    });
  });
})(jQuery);