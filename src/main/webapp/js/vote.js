$(function() {
    var VOTES = 10,
      update = function(ids) {
        var remaining = VOTES - ids.length;
        $("#remaining").text(remaining == 1 ? "1 vote " : remaining + " votes ");
        $(".entry a.vote").each(function(elem) {
            if ($.inArray(parseInt(this.id.substring(1)), ids) > -1) {
                if(!$(this).hasClass("voted")) {
                  $(this).text("Voted").addClass("voted");
                }
            } else {
                $(this).text("Vote");
            }
        });
      };

    $("a.vote").hide();

    $.post("/vote", function(ids) {
      update(ids);
      $("a.vote").fadeIn(500).live("click", function(e) {
        e.preventDefault();
        if(!$(this).hasClass("voted")) {
          $.post("/vote", {
            entry_id: this.id.substring(1),
            action: $(this).text()
          }, update);
        }
      });
    });
});
