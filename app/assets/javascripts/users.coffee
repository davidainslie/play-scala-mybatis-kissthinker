$("#users").bind "click", (event) =>
    $ ->
        $.get "/users", (users) ->
            $("#content").html("<ul id='usersList' style='color: white'></ul>")

            $.each users, (index, user) ->
                $("#usersList").append("<li>#{user.id}, #{user.firstName}, #{user.lastName}</li>")