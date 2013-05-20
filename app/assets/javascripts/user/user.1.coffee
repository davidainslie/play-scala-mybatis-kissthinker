$("#user1").click ->
    jqxhr = $.get "/users/1", (user) ->
        $("#content").html("<h3 style='color: white'>User ID: #{user.id}</h3>")
    .done( -> alert("Amazing Done"))
    .fail( -> alert("Amazing Fail"))