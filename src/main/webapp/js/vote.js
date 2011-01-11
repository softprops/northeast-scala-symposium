$(function() {
    var update = function(ids) { 
        $("#remaining").text(5 - ids.length);
        $(".entry a.vote").each(function(elem) {
            if ($.inArray(parseInt(this.id.substring(1)), ids) > -1) {
                $(this).text("Undo");
            } else {
                $(this).text("Vote");
            }
        });
    }
    $("a.vote").live("click", function(e) {
        e.preventDefault();
        $.post("/vote", { 
            entry_id: this.id.substring(1), 
            action: $(this).text()
        }, update);
    });
    $.post("/vote", update);
});
