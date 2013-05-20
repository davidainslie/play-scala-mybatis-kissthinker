$("#users").click ->
    $.post "/users", (users) ->
        $("#content").html("<ul id='usersList' style='color: white'></ul>")

        $.each users, (index, user) ->
            $("#usersList").append("<li>#{user.id}, #{user.firstName}, #{user.lastName}</li>")
    .fail( -> alert("Amazing Fail"))


$ ->
    $("#userSearchForm").submit ->
        $.post($(this).attr("action"), $(this).serialize(), (users) ->
            $("#content").html("<ul id='usersList' style='color: white'></ul>")

            $.each users, (index, user) ->
                $("#usersList").append("<li>#{user.id}, #{user.firstName}, #{user.lastName}</li>")
        )
        .fail( -> alert("Amazing Fail"))

        false