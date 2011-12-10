(function($){
  var tallies = $("#tallies"), total = parseInt(tallies.attr("data-total")),
      px = tallies.width() / 100;
  $(tallies).children().each(function(i,e) {
     var entry = $(e), votes = parseInt(entry.attr("data-score"));
     entry.click(function(){
          window.location.href=window.location.href.replace("tally","vote")+"#"+e.id;
      });
      entry.find(".bar").animate({"width": (votes * px) + "px"}, 500);
  });
})(jQuery);