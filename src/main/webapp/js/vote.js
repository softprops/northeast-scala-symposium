$(function() {
    var update = function(ids) { 
        $("#remaining").text(5 - ids.length)
    }
    $("div.entry a").click(function(e) {
        e.preventDefault();
        $.post("/vote", { entry_id: $(this).attr("class").substring(1) }, update);
    });
    $.post("/vote", update);
});
