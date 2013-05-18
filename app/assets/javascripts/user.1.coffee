$("#user1").click ->
    $.get "/users/1", (user) ->
        $("#content").html("<h3 style='color: white'>User ID: #{user.id}</h3>")