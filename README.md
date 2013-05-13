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

There are other versions of this application, which instead of using MyBatis use:
<ul>
    <li><a href="https://github.com/davidainslie/play-scala-hibernate-kissthinker">Hibernate</a></li>
    <li><a href="https://github.com/davidainslie/play-scala-mongo-casbah-salat-kissthinker">Mongodb with Casbah and Salat</a></li>
    <li><a href="https://github.com/davidainslie/play-scala-reactive-mongo-kissthinker">Mongodb with ReactiveMongo</a></li>
</ul>

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

```scala
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

```scala
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

```scala
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

```scala
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

Here we have two main divs.
The last one is pretty straight forward - it's empty. This will be where all content is set for our single web page application.
The first div is a Twitter Bootstrap navigation bar. It's mainly Bootstrap stuff apart from the "script".
Our single web page application works with Ajax/jQuery/JavaScript, which will make calls to our server and receive JSON.

So, Ajax?

We want to make a call to get, as JSON, the User with ID of 1. To make the call we do:
```html
$.getJSON("/users/1", <plus a callback function to handle received JSON>)
```

The call will be issued upon clicking the link with (CSS) id "user1".
Note we've wrapped the clicking and Ajax call inside "$(function() ...". This is just asking jQuery to run (setup) everything inside upon page load.
And finally upon receiving JSON, we use the results to set our single web page "content".

Now the spec example above should be more readable.
With the given "browser", go to the main page. Click the nav bar to ask for "user 1", and assert the page result.

And what is that "new WithChromeBrowser"?
We are testing/driving our web browser with Selenium. Selenium need a web browser driver - if we used the default, the declaration would be "new WithBrowser".
However, the default driver, doesn't seem to like jQuery. Running with the default gives the exception: "Cannot find function addEventListener".
Normally, concerning jQuery, I use the Firefox web driver which is included in Play. However, on one of my PCs it hangs!

For this example, and for the first time, I've used the Chrome web driver (which I downloaded into the root of this project).
Hence the WithChromeBrowser, an extension of WithBrowser, to load the Chrome web driver.
Annoyingly, when running our specs with this driver, a popup dialog appears where we have to manually accept terms and conditions.
I tried out the new (currently alpha) version of the driver, ChromeDriver2, and here we don't get any popup dialog, but it can be slow (can't decide which to use).

Upon getting backing to a "green light", should be refactor now? Currently we are returning a hard coded user and not yet interacting with MyBatis.
It's a matter of choice really. I'm going to code one more example first, to get a list of all users (which again will be hardcoded).
And after refactoring, I'm going to show alternatives to Ajax/JavaScript, namely Ajax/CoffeeScript.

Here's the new example in UsersSpec:

```scala
"view all users" in {
  val request = FakeRequest().withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
  val result = Users.view()(request)

  status(result) mustEqual OK
  contentType(result) must beSome("application/json")
  charset(result) must beSome("utf-8")

  import play.api.libs.json.Json
  implicit val userFormat = Json.format[User]

  val users = Json.parse(contentAsString(result)).as[List[User]]

  users.size mustEqual 3
}
```

Almost identical to viewing a single user. This time we expect to get back a list of User in JSON format, consisting of 3 users.

The new implementation, in Users, got me from red to green in one go (trust me):

```scala
def view = Action {
  implicit val userFormat = format[User]

  val users = User(1) :: User(2) :: User(3) :: Nil
  Ok(toJson(users))
}
```

And again, it's almost identical to our previous hardcoded implementation.

Finally onto some refactoring. We shall now bring in MyBatis by starting, as usual, with an example:

```scala
class UserDAOSpec extends Specification {
  "UserDAO" should {
    "insert new User which will be assigned next available ID, in this case 1" in {
      val user = UserDAO.save(User(firstName = "Scooby", lastName = "Doo"))
      user.id mustEqual 1
    }
  }
}
```

To get from red to green, I need to:
- Implement the new UserDAO and
- Re-implement User

Scala has so many good things (over Java) and here is just one. The current User class takes an ID, but now we want that ID set by something else (like the DAO) and also give a name.
Why include name now and not before? Before it was not needed, just ID, but now we are not providing ID, so I want to provide something and so I've chosen name (a fairly obvious choice).
However, the new implementation will break my current examples. Nop! Because Scala allows us to set defaults and provide arguments by name, we avoid that (just now anyway).
This is handy in this situation, where we can concentrate on the current example and not have to immediately fix others.

And here is the amended User:

```scala
case class User(id: Long = 0, firstName: String = "", lastName: String = "")
```

We're going to keep User immutable as per good Scala practice - well should be good practice in any language.

As for our new DAO, again stick with what is immediately required. Upon a save, we should get back a User with ID of 1, so we hardcode again.
If you've not already thought about it, maybe you should now question all this hardcoding!
Just one point of writing lots of examples (tests) is that eventually one will come along where you will have to refactor and remove the hardcoding -
<b>your design simple unravels</b>.

```scala
object UserDAO {
  def save(user: User) = User(1)
}
```

What a superb DAO implementation! Hey! We are green again!
Ok, time to refactor the hardcoding!
So here is the next stage of our DAO, which is specific to MyBatis i.e. even though the public interface is datastore agnostic, we cannot swap out a different implementation such as Hibernate.
But what about mocking? With Play, in this case, we won't need mocking - the example and code are self contained, so why mock?
This new implementation is quite a rewrite, but still within the realms of manageability - explanation to follow:

```scala
object UserDAO {
  def bind = Seq(insert)

  def save(user: User): User = inTransaction { implicit session =>
    val userEntry = new UserEntry(user)
    insert(userEntry)
    user.copy(id = userEntry.id)
  }

  private val insert = new Insert[UserEntry] {
    keyGenerator = JdbcGeneratedKey(null, "id")

    def xsql = <xsql>
      insert into user (first_name, last_name)
      values ({"first_name"?}, {"last_name"?})
    </xsql>
  }

  class UserEntry(user: User) {
    var id : Long = user.id

    var first_name : String = user.firstName

    var last_name : String = user.lastName
  }
}
```

The public interface is "save". Fair enough, we want to save a User.
What we have done is code the actual database insertion code, in a separate method named "insert".
This was chosen because MyBatis provides classes such as "Insert" and "Update" and we anticipate that "save" will have to make the choice of inserting or updating.
Now haven't we just coded something that is not required at this time? Maybe, but I also wanted to separate "save" from "insert" because "save" deals with (is given) a User, whereas "insert" deals with (is given) a UserEntry.

So what is UserEntry, if we already have a User? I've added this, because there are a few annoying things about MyBatis (even though it is an excellent product).
Of course the same can be said for other technologies, so I'm not going to worry, but when writing my spec/example, I wanted User to be immutable.
MyBatis (and again other persistence libraries) doesn't have a nice fit - the object that is given to "insert" method is actually mutated.
So I've coded UserEntry, a mutable version of User and representing an actual entry in the User table of the database.

The rest of the code is essentially working with the <a href="http://mybatis.org/scala/">MyBatis-Scala</a> library.
Though note, I have added the object MyBatis as kind of a little helper, such as using the method "inTransaction".

Just a quick followup note on how we can avoid the use of mocking, because this example feels like it should be an "integration test".
We've actually updated our spec with one small addition, namely the use of "WithServer":

```scala
class UserDAOSpec extends Specification {
  "UserDAO" should {
    "insert new User which will be assigned next available ID, in this case 1" in new WithServer {
      val user = UserDAO.save(User(firstName = "Scooby", lastName = "Doo"))
      user.id mustEqual 1
    }
  }
}
```

Because of "WithServer" our example will start up a server embedded within our "test environment".
Play then configures itself e.g. read the file "application.conf" where many configurations can be declared including datasources.
A new Play application will automatically configure a datasource for an in memory database - we just have to uncomment the declaration an use it.
Take another look at the helper object MyBatis which uses the "default" datasource.

And what about "seed data" for these "database tests"? Play sorts all of that for us as well, with "evolutions".
Under the directory "conf/evolutions/default" are some SQL which will be run for us - create tables; seed data; drop tables (though this isn't required for an in-memory database).

