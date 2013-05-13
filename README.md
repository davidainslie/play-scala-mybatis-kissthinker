Front to back (simple demo) application showing how to use/approach the following:

<ul>
	<li>Scala (version 2.10.1)</li>
	<li>Play (2.1.0)</li>
	<li>Web technologies including Ajax, JavaScript, JQuery, Twitter Bootstrap, HTML, CSS and some CoffeesScript</li>
	<li>BDD with Specs2 1.13</li>
	<li>BDD (Scala/Play) web application with FluentLenium (0.8.0)</li>
	<li>MyBatis (using mybatis-scala-core version 1.0.1)</li>
</ul>

Regarding the utilised technologies, all dependencies can be seen in <root>/project/Build.scala
We will be taking a small tangent from a traditional Play application and code a "single web page application" (instead of the usual multiple pages).
Note that to easily follow the explanations below, I'm assuming that you are not a complete beginner in anything mentioned.

Upon cloning this project (or downloading and decompressing the zip) you can run the application (before viewing the code) if you have Play installed.
Don't have Play (or just need more information) then goto:
http://www.playframework.com
and
http://www.playframework.com/documentation/2.1.1/Installing (where as of writing, the current version of Play is 2.1.1)

Even though this application was written using BDD (Behaviour Driven Development) and not DDD (Domain Driven Development), before discussing the specs (tests) and implementation, here is the (domain) model to ease your understanding of what is going on.

![Alt Domain Model](/doc/model.png "(Domain) Model")

Why this model?
It is simple but at the same time shows composition and inheritance, which keeps the application more real, especially since most applications have the concept of "users".

At a high level, this simple demo has a login page which will take a user to their profile page.
If the logged in user is an "administrator" then that person's home page shows all users (including who is currently logged in).
Anyone can look up a user, as a (found) user can be added as an associate/friend/colleague (whatever).
A user can edit their own profile - An administrator is given the option to delete a user.

So how does it all fit together?
Let's rewind to when I started the application with BDD.

A first story: User should view a (some) user's profile.
What this entails, is to find the appropriate user, let's start with the usual, looking up by ID.
The user (object) will be stored in some datastore (for our unit specs, which are pretty much the same as unit test we shall use the H2 "in memory" database that is configured in Play.
Our first example (essentially a test method) could assert against a "get" method on a "user DAO", but that has been done a million times before.
We shall dive in with an example that actually goes through Play, specifically, when a request comes into the application, the first port of call is a Controller (think of this as a fine-grained Servlet).

So, the first spec and example to request for user with ID of 1, and assert that the result is a JSON representation of User, which has ID of 1:

```java
class UsersSpec extends Specification {
  "User" should {
    "view a user profile" in {
      val request = FakeRequest().withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      def result = Users.view(1)(request)

      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      charset(result) must beSome("utf-8")

      import play.api.libs.json.Json
      implicit val userFormat = Json.format[User]

      val user = Json.parse(contentAsString(result)).as[User]

      user.id mustEqual 1
    }
  }
}
```

Note that I'm not showing the imports, at the top of a file anyway, just to keep this text a tad more concise.
You can of course see all the code/imports under "src".

Initially the above will not compile. Since we are BDD and write our specs/examples first, we next have to code an implementation.
The first implementation should fail, if we apply the traditional TDD method of "red light, green light, refactor (if appropriate)":

```java
object Users extends Controller {
  def view(id: Long) = Action {
    Ok(Json.obj("id" -> -1) )
  }
}
```

Our first implementation is hardcoded, with the minimum required.
In fact, we have directly created the JSON to send back as a response.
The initially coded User object just has as "id" and no relationships, again to make our code compile (as red).
To then get a "green" example, we just change the returned "id" to be 1.

And next we decide if any refactoring would be appropriate. I think so.
We have a User object, so let's use that an "auto" generate JSON, just as we showed what we wanted from our example (which was to "auto" parse JSON):

```java
object Users extends Controller {
  def view(id: Long) = Action {
    implicit val userFormat = format[User]

    val user = User(1)
    Ok(toJson(user))
  }
}
```

We are now going to write an equivalent integration spec - it's always good to code your integration specs as early as possible (even better in sync with the unit specs).
Play helps you to code "browser simulation tests" with Selenium. Play autogenerates IntegrationSpec as an example.
Play also autogenerates a "main" HTML page, which we are going to turn into a "single web page application".

Here is the original main page:

```html
@(title: String)(content: Html)

<!DOCTYPE html>

<html>
    <head>
        <title>@title</title>
        <link rel="stylesheet" media="screen" href='@routes.Assets.at("stylesheets/main.css")'>
        <link rel="shortcut icon" type="image/png" href='@routes.Assets.at("images/favicon.png")'>

        <script src='@routes.Assets.at("javascripts/jquery-1.9.0.min.js")' type="text/javascript"></script>
    </head>

    <body>
        @content
    </body>
</html>
```

The above already has a "single web page application" feel to it, as the idea is that all your HTML/template pages will replace "@content" for a consistent layout.

So we have our page already in place, now we can write the integration spec. Mmm... That's the wrong ways round for BDD.
No matter, as mentioned we are going to re-implement the above page. Following is our spec to once again view User with ID of 1.

```java
class UsersIntegrationSpec extends Specification {
  "User" should {
    "view a user profile" in new WithChromeBrowser {
      browser.goTo("/")
      browser.title() mustEqual "Home"
      browser.click("#usersButtonGroup")
      browser.click("#user1")
      browser.find("#content") contains "User ID: 1"
    }
  }
}
```

This example, "view a user profile", essentially simulates how we anticipate interacting with the browser.
I usually put together (a rough) HTML page at the same time as coding the spec/example.
Doing these at the same time is of course not technically BDD (or TDD).
However, as HTML is not really code, and the specs/examples in this case relate to something viewable, is is easier to envisage what the code should be with some associated HTML.
We throw some HTML together, and can even do some CSS at this point, open the page in a browser (independently i.e. no frameworks/containers/servers running), and write our code.

So before covering the above example, let's have a look at my first, thrown together, page (well, just he body to save space):

```html
<body>
    <div id="nav">
        <div class="btn-group">
            <a id="usersButtonGroup" class="btn dropdown-toggle" data-toggle="dropdown" href="#">
                Users
                <span class="caret"></span>
            </a>

            <ul class="dropdown-menu">
                <li>
                    <a id="user1" href="#">View User with ID of 1</a>

                    <script>
                        $(function() {
                            $("#user1").click(function() {
                                $.getJSON('/users/1', function(user) {
                                    $("#content").html("<h3 style='color: white'>User ID: " + user.id + "</h3>");
                                });
                            });
                        });
                    </script>
                </li>
            </ul>
        </div>
    </div>

    <div id="content">
    </div>
</body>
```