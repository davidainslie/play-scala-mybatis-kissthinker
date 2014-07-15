Demo/Tutorial of Scala/Play/MyBatis Application
===============================================

Front to back demo/tutorial application of taking a BDD approach to Scala stack technologies with MyBatis on the backend
------------------------------------------------------------------------------------------------------------------------

Application built with the following technologies:

- Scala 2.10.1

- SBT 0.12.2

- Play 2.1.1

- MyBatis 1.0.1

- Specs2 1.13

and including

- HTML, CSS, JavaScript/CoffeeScript, JQuery, Ajax, Twitter Bootstrap

Introduction
------------

A simple application that concentrates on the overall end-to-end process - the main goal being to take you through a BDD approach when creating a web application built on top of Scala stack technologies.
As a result, the UI is fairly non-existent (it only shows the bare minimum) and "model" being processed and save on the backend is simply "users" and their "profiles".

Regarding the utilised technologies, all dependencies can be seen in <root>/project/Build.scala
We will be taking a small tangent from a traditional Play application and code a "single web page application" (instead of the usual multiple pages).
Note that to easily follow the explanations below, I'm assuming that you are not a complete beginner in anything mentioned.

There are other versions of this application, which instead of using MyBatis use:
<ul>
    <li><a href="https://github.com/davidainslie/play-scala-hibernate-kissthinker">Hibernate</a></li>
    <li><a href="https://github.com/davidainslie/play-scala-mongo-casbah-salat-kissthinker">Mongodb with Casbah and Salat</a></li>
    <li><a href="https://github.com/davidainslie/play-scala-reactive-mongo-kissthinker">Mongodb with ReactiveMongo</a></li>
</ul>

Setup
-----

Upon cloning this project (or downloading and decompressing the zip) you can run the application (before viewing the code) if you have Play installed.
Don't have Play (or just need more information) then goto:
http://www.playframework.com
and
http://www.playframework.com/documentation/2.1.1/Installing (where as of writing, the current version of Play is 2.1.1)

Specs
-----
First take a look at the specs.
They can be run individually in your IDE or from SBT command line console using the "test" command.
There are both unit and integration specs, where the integration specs often use an embedded Mongodb.

Application
-----------

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

A first story: User should view a/some user's profile.
What this entails, is to find the appropriate user, let's start with the usual, looking up by ID.
The user (object) will be stored in some datastore (for our unit specs, which are pretty much the same as unit test we shall use the H2 "in memory" database that is configured in Play.
Our first example (essentially a test method) could assert against a "get" method on a "user DAO", but that is kind of the norm, so we'll save that for later.
We shall instead dive in with an example that actually goes through Play, specifically, when a request comes into the application, the first port of call is a Controller (think of this as a fine-grained Servlet).

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
Just one point of writing lots of examples (tests) is that eventually one will come along where you will have to refactor and remove the hardcoding -<br/>
<b>your design simply unravels</b>.

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

Let's add one more example before moving back to doing some more Ajax/Javascript and introducing CoffeeScript.

Now I wanted to write an example to view a list of all users i.e. seed the in memory database with say 4 users and read them in.
But then I thought, just because I'm now using WithServer, will another example, with it's own embedded server still use the same MyBatis "context"?
Remember I mentioned the MyBatis helper? Well we have an issue! And it's about time to. Now we'll have to do some pretty big refactoring, but hey, we have specs.

Here is the original helper:

```scala
object MyBatis {
  private val configuration = createConfiguration()

  private val sessionManager = configuration.createPersistenceContext

  def inTransaction[R](f: Session => R): R = sessionManager.transaction(f)

  private def createConfiguration() = {
    val configuration = Configuration(Environment("default", new ManagedTransactionFactory(), getDataSource()))
    configuration ++= UserDAO
    configuration
  }
}
```

It's an object (singleton for all you Java developers).
The issue is that when the first example runs, the "vals" are initialised when MyBatis is first accessed.
After the example, the actual MyBatis library shuts down its database connection pool. Upon running the next example, an exception is thrown, complaining about the shutdown pool.
To prove this to myself, I wrote a duplicate example (which I've left in).

Moral of the story is - refactor.
We shall do two large refactorings:
<ol>
    <li>object MyBatis becomes a trait, to allow mixing in of its functionality.</li>
    <li>object UserDAO becomes a class so we can mixin the MyBatis trait - by doing this, we can now instantiate the DAO for each example and so create a new connection pool each time.</li>
    <li>and then a bit of a clean up: rename MyBatis to DAO and move it to new package "mybatis"; get rid of "def bind" in UserDAO since it is now given the DAO (formally MyBatis) functionality where we can access its "configuration".</li>
</ol>

Our refactored DAO (formally MyBatis) now looks like:

```scala
trait DAO {
  protected val configuration = Configuration(Environment("default", new ManagedTransactionFactory(), getDataSource()))

  private val sessionManager = configuration.createPersistenceContext

  def inTransaction[R](f: Session => R): R = sessionManager.transaction(f)
}
```

And our refactored UserDAO look like:

```scala
class UserDAO extends DAO {
  configuration ++= Seq(insert)

  def save(user: User): User = inTransaction { implicit session =>
    val userEntry = new UserEntry(user)
    insert(userEntry)
    user.copy(id = userEntry.id)
  }

  private lazy val insert = new Insert[UserEntry] {
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

Ok! UserDAO hasn't changed that dramatically but these are big changes going from object to class and mixing in. The good news is that we are still green!
Note that with all this refactoring going on, we could have now introduced a UserDAO interface. However, we are still (always) only doing what is necessary.
Oh! And an interesting point, we had to declare "insert" method as "lazy" - without this, the call to "configuation ++= Seq(insert)" would need to come after the insert method or MyBatis would throw a wobbly.

Now where were we? All users example! Here it is - first the DAO now has:

```scala
"find all User" in new WithServer {
  val users = new UserDAO().all

  users must contain(User(3, "George", "Harrison"),
                     User(4, "Ringo", "Starr"),
                     User(2, "John", "Lennon"),
                     User(1, "Paul", "McCartney")).only
  }
```

How does the DAO retrieve data from the test/embedded database? When our examples are run, we have SQL scripts executed, in their numerically named order - see scripts:
conf/evolutions/default/1.sql and 2.sql

I've also updated the original examples in this spec to account for the fact that there is now "seeded data".

The UserDAO has had the following added to get to green:

```scala
def all: List[User] = inTransaction { implicit session =>
  findAll().map(u => User(u.id, u.first_name, u.last_name)).toList
}

private lazy val findAll = new SelectList[UserEntry] {
  def xsql = "select id, first_name, last_name from users"
}
```

And we quickly (we are picking up pace) move onto adding an example in UsersSpec and relevant implementations to view all users.
I've coded a second version of the following new spec just to show JSON matchers in Specs2, since front and back ends are communicating in JSON, though this version wouldn't really help us.

```scala
"view all users" in new WithServer {
  val request = FakeRequest().withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
  val result = Users.view()(request)

  status(result) mustEqual OK
  contentType(result) must beSome("application/json")
  charset(result) must beSome("utf-8")

  import play.api.libs.json.Json
  implicit val userFormat = Json.format[User]

  val users = Json.parse(contentAsString(result)).as[List[User]]

  users.size mustEqual 4
}

"view all JSON users" in new WithServer {
  val request = FakeRequest().withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
  val result = Users.view()(request)

  status(result) mustEqual OK
  contentType(result) must beSome("application/json")
  charset(result) must beSome("utf-8")

  val users = contentAsString(result)

  users must */("id" -> 2)
  users must */("firstName" -> "John")
  users must */("lastName" -> "Lennon")
}
```

So the Users has been updated for green as:

```scala
object Users extends Controller {
  val userDAO = new UserDAO()

  def view = Action {
    implicit val userFormat = format[User]

    val users = userDAO.all
    Ok(toJson(users))
  }

  def view(id: Long) = Action {
    implicit val userFormat = format[User]

    val user = User(1)
    Ok(toJson(user))
  }
}
```

As for the actual "browser" spec, we again want to view all users and expect them to be added in the web page via an Ajax call. The new example in UsersIntegrationSpec is:

```scala
"view all users" in new WithChromeBrowser {
  browser goTo "/"
  browser title() mustEqual "Home"
  browser click "#usersButtonGroup"
  browser click "#users"

  browser.waitUntil[Boolean](3, TimeUnit.SECONDS) {
    browser pageSource() contains "Lennon"
  }

  browser find "#usersList" getText() must contain("2, John, Lennon")
}
```

In this integration spec (I've just realised that some may rather call this an acceptance spec as it simulates acceptance testing by an end user)...
I'll try again. In this integration spec, I've added a wait - since an Ajax call is being performed, without the wait, we will "assert" before the JSON results are returned.
Waiting is pretty ugly and I'd rather not do it. How long should we wait? I've guessed.
And the moment I can't think of another way. In the more "traditional" async/concurrent test, I would use something along the lines of a CountDownLatch.

Now we've added another "nav" link to our main.scala.html, to make the relevant Ajax call.
With this second nav, the page is already looking ugly. We have a green light, so we can refactor. Time to dabble in CoffeeScript.

By the way, here is the newly added Ajax/Javascript call (and I know we could extract this out into a separate file):

```html
<li>
    <a id="users" href="#">View All Users</a>

    <script>
        $(function() {
            $("#users").click(function() {
                $.getJSON('/users', function(users) {
                    $("#content").html("<ul id='usersList' style='color: white'></ul>");

                    $.each(users, function(index, user) {
                        $("#usersList").append("<li>" + user.id + ",&nbsp;" + user.firstName + ",&nbsp;" + user.lastName + "</li>");
                    });
                });
            });
        });
    </script>
</li>
```

A quick word on CoffeeScript.
It's to JavaScript, what Groovy is to Java - well, kind of, but this is a handy thought.
When writing CoffeeScript, you can almost write JavaScript, but just like Groovy, CoffeeScript can be so much more succinct and palatable.

So, let's extract the Ajax/JavaScript and at the same time introduce CoffeeScript, which essentially makes the Javascript more readable and functional, in line with our Scala code.
The updated "nav" now refers to external JavaScript files (which are actually auto compiled, by Play, from our CoffeeScript files):

```html
<ul class="dropdown-menu">
    <li>
        <a id="users" href="#">View All Users</a>
        <script type="text/javascript" src='@routes.Assets.at("javascripts/users.js")'></script>
    </li>

    <li>
        <a id="user1" href="#">View User with ID of 1</a>
        <script type="text/javascript" src='@routes.Assets.at("javascripts/user.1.js")'></script>
    </li>
</ul>
```

Ah! Good! Readable again. And here are the two new CoffeeScript files, which are stored under "app/assets/javascripts":

To get all users (as JSON) and process:

```coffeescript
$("#users").click ->
    $.get "/users", (users) ->
        $("#content").html("<ul id='usersList' style='color: white'></ul>")

        $.each users, (index, user) ->
            $("#usersList").append("<li>#{user.id}, #{user.firstName}, #{user.lastName}</li>")
```

To get user with ID 1 (as JSON) and process:

```coffeescript
$("#user1").click ->
    $.get "/users/1", (user) ->
        $("#content").html("<h3 style='color: white'>User ID: #{user.id}</h3>")
```

With all the above spec/examples/implementation in place, maybe we should try running the application and interact with a browser?
Even though we are BDD, Play is remarkable when it comes to running the server, changing code, and seeing the changes in the browser.

And with Evolutions, you will be constantly in awe of Play e.g. upon running the Play application with:

```scala
> play run
```

then open a browser page at http://localhost:9000, we are presented with a request from Evolutions to set up the database:

![Alt Evolutions - Run Scripts](/doc/evolutions.png "Evolutions - Run Scripts")

A great example of doing dynamic changes in your running Play application, is when I wanted to view all users. The screen was empty!
What gives? I opened up Chrome's "Developer Tools" to see the DOM of the page, and there were the users.
However, I had black text on a black background. These days you can change this in the browser, but to keep your changes you still have to update your file(s).
So I opened up the relevant HTML page; added a style for white text; refreshed the browser; navigated back to view all users; and there they were!

Currently the UI is pretty naff, as shown by these screenshots, but as yet we've not really spent much time here - we'll get there.

![Alt Screenshot 1](/doc/screenshot1.png "Screenshot 1")

![Alt Screenshot 2](/doc/screenshot2.png "Screenshot 2")

<p/>
Time to carry on coding.
<p/>
As we last reached green I decided to do some refactoring, specifically UserDAO. I really hated that extra class "UserEntry".
I went back to this class knowing that MyBatis has "result map" functionality, allowing up to, well map results to our domain object - so that is what I did.
After the refactor, all specs were rerun to make sure that we were still green. Not only are we still green but now I like MyBatis once again.
The code is clean/readable and we are in complete control of our SQL, which is kind of the point of MyBatis.

And here is the current refactored DAO in all its glory (excluding top level imports for conciseness of course):

```scala
class UserDAO extends DAO {
  configuration ++= Seq(insert, findAll)

  def save(user: User): User = inTransaction { implicit session =>
    insert(user)
    user
  }

  def all: List[User] = inTransaction { implicit session =>
    findAll().toList
  }

  private def userResultMap = new ResultMap[User] {
    idArg(column = "id", javaType = T[Long])
    arg(column = "first_name", javaType = T[String])
    arg(column = "last_name", javaType = T[String])
  }

  private lazy val insert = new Insert[User] {
    keyGenerator = JdbcGeneratedKey(null, "id")

    def xsql = <xsql>
      insert into users (first_name, last_name)
      values ({"firstName"?}, {"lastName"?})
    </xsql>
  }

  private lazy val findAll = new SelectList[User] {
    resultMap = userResultMap

    def xsql = "select id, first_name, last_name from users"
  }
}
```

A small "gotcha" in the above code.
The "userResultMap" must be declared as "def" and not "val", as a "def" is resolved everytime (val is like a Java "final" and set only once).
This gotcha is along the sames lines as having to use "lazy" on our "SQL" functionality.

We still have a piece of hard coding - get user 1. After sorting this one out we can handle the case of saving/updating an existing user.
Let's update UserDAOSpec to find a user given the ID (something that we shall later confine to only "admin" functionality).

We'll need two new examples - cannot find non-existing user, and find an existing user.
Doesn't matter which we choose first, but often it is the failing case i.e. the next code snippet can be:

```scala
"UserDAO" should {
  // ...

  "not find non-existing User for an invalid ID" in new WithServer {
    new UserDAO().find(-1) must beNone
  }
}
```

And we are red... well, as usual because of a compilation error.
We initially fix that with a hard coded implementation of course.
However, this one will immediately give green (going against the rules of a genuine red after successful compilation - but there are just no two ways to this first hardcoding, so let's live with it).

```scala
def find(id: Long): Option[User] = None
```

But now for the passing example:

```scala
"UserDAO" should {
  // ...

  "find user by ID" in new WithServer {
    new UserDAO().find(1) must beSome(User(1, "Paul", "McCartney"))
  }
}
```

Aha! Genuine red (because of a code break - our hardcoding, and as mentioned, hardcoding is always factored out when more examples are added).
The new implemenation needs some more of the MyBatis/SQL stuff:

```scala
  def find(id: Long): Option[User] = inTransaction { implicit session =>
    findById(id)
  }

  private lazy val findById = new SelectOneBy[Long, User] {
    resultMap = userResultMap

    def xsql = <xsql>select * from users where id = {"id"?}</xsql>
  }
```

Hopefully you'll getting used to what's going on, and at the same time understanding how to use MyBatis with Scala. It is now looking easy. Will it stay that when is comes to relationships?

Let's now update the UsersSpec to really find user 1. We are going to achieve this by adding a new failing example i.e. request to find a non-existing user.
However, once again, since we are green we can refactor (and re-run all specs).

Our UsersSpec has quite a bit of repetition regarding firstly asserting that we are getting JSON formatted data i.e.

```scala
status(result) mustEqual OK
contentType(result) must beSome("application/json")
charset(result) must beSome("utf-8")
```

and secondly parsing said JSON into a domain object i.e.

```scala
import play.api.libs.json.Json
implicit val userFormat = Json.format[User]

val user = Json.parse(contentAsString(result)).as[User]
```

The first case can be amended with the new "matcher" (that can be mixed into UsersSpec):

```scala
trait JSONMatcher extends Specification {
  implicit def resultToJSONMatcher(result: Result) = new JSONMatcher(result)

  class JSONMatcher(result: Result) {
    def isJSON = {
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      charset(result) must beSome("utf-8")
    }
  }
}
```

and the second case with another trait for mixing in:

```scala
object User {
  trait JSON {
    import play.api.libs.json.Json

    implicit val format = Json.format[User]

    def parse(json: String) = Json.parse(json)
  }
}
```

Now let's add our new example, also showing the use of the above two new traits:

```scala
class UsersSpec extends Specification with JSONMatcher with User.JSON {
  "User" should {
    // ...

    "get an error for a non existing user request" in new WithServer {
      val request = FakeRequest().withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      val result = Users.user(-1)(request)

      status(result) mustEqual NOT_FOUND
    }
```

To get green we need to update the Users controller.
And while we're at it, why not update the Users controller to also have User.JSON mixed in to again removed redundant/boilerplate code:

```scala
object Users extends Controller with User.JSON {
  def users = Action {
    val users = new UserDAO().all
    Ok(toJson(users))
  }

  def user(id: Long) = Action {
    new UserDAO().find(id) match {
      case Some(u: User) => Ok(toJson(u))
      case _ => NotFound
    }
  }
}
```

(Note that with this new implementation, we could also update the example "view a user profile" to be more specific in its assertion).

Regarding this new story (find/search a user) we finally arrive at UsersIntegrationSpec.
What we really want, is a search form. We shall fill this in the spec, but let's see how the form should look, after all the UI is currently a load of ****.
We shall carry on using CoffeeScript to put together the form. This is quite an interesting one, as we can construct all HTML on multiple lines, but be careful, just like the likes of Python, CoffeeScript is sensitive to indentation (preferring indentation over semi-colons).

Our first (what could be regarded as hardcoded) version simply gives a first draft of how we think the form will look like including:
<ul>
    <li>Quick improvements to the look and feel (as shown in the below screenshots).</li>
    <li>Use of Twitter Bootstrap's form style with minor overrides.</li>
    <li>Currently assuming that anyone can search by ID, but later on this will be changed so that this input field is only viewable by "admin".</li>
    <li>The filter... well as yet, that is ad-hoc and will probably be based on regex - with the optional "case sensitive" checkbox.</li>
</ul>

So, the new CoffeeScript, userSearch.coffee looks like:

```coffeescript
$("#userSearch").click ->
    $("#content").html """
        <form class="form-horizontal">
            <legend>User Search</legend>

            <div class="control-group">
                <label class="control-label" for="id">ID</label>

                <div class="controls">
                    <input type="text" id="id" placeholder="ID">
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="filter">Filter</label>

                <div class="controls">
                    <input type="password" id="filter" placeholder="Filter">
                </div>
            </div>

            <div class="control-group">
                <div class="controls">
                    <label class="checkbox">
                        <input type="checkbox"> Case Sensitive
                    </label>

                    <button type="submit" class="btn">Search</button>
                </div>
            </div>
        </form>
        """
```

Very nice!
We have written standard HTML, and when the time comes (when clicking "User Search") our single page web application dynamically updates the document.
And the triple quotes, along with functional style, all looks a bit kind of Scala... even nicer!

And a few screenshot updates:

![Alt Screenshot 1](/doc/screenshot-a1.png "Screenshot 1")

![Alt Screenshot 2](/doc/screenshot-a2.png "Screenshot 2")

![Alt Screenshot 3](/doc/screenshot-a3.png "Screenshot 3")

Before proceeding, let's refactor. Even though the last CoffeeScript was a nice proof of concept, it is not the most concise (and testable) way of using Ajax.
So we shall extract out the "form" into its own HTML file (which allows up to directly open this new page to easily view it).
As we are in the Play world, we shall actually extract into a file of type ".scala.html" which allows the new HTML file to be compiled into a Scala function (which is what Play does - check out their website).
Being a ".scala.html" file, we place it under the "views" directory instead of the "public" directory, and then to access the file (within the Play world) we have to add a new route in "routes".

Ok, enough chit-chat, here are the refactorings (including moving files around into new directories to keep things clean):

The above CoffeeScript now becomes (and renaming to search.coffee user directory "user"):

```coffeescript
$("#userSearch").click ->
    $("#content").load "/users/search"
```

Nice! The load itself is picked up by our new route:

```scala
GET     /users/search               controllers.Users.search
```

which is essentially mapped to the new "search" method in Users:

```scala
def search = Action {
  Ok(views.html.user.search())
}
```

And finally, the extracted HTML form is now in "search.scala.html" (where the file starts as a function declaration i.e. the part "@()" meaning it is a parameterless function):

```html
@()

<form class="form-horizontal">
    <legend>User Search</legend>

    <div class="control-group">
        <label class="control-label" for="id">ID</label>

        <div class="controls">
            <input type="text" id="id" placeholder="ID">
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="filter">Filter</label>

        <div class="controls">
            <input type="password" id="filter" placeholder="Filter">
        </div>
    </div>

    <div class="control-group">
        <div class="controls">
            <label class="checkbox">
                <input type="checkbox"> Case Sensitive
            </label>

            <button type="submit" class="btn">Search</button>
        </div>
    </div>
</form>
```

Run all specs/examples to check on the refactoring.

At this time, the search form doesn't do anything yet. Lately we have been "visual", but we should get back to BDD.
The visual part was essentially hardcoded - on a web project you are often given a hardcoded piece of HTML from a "page designer" and now we must implement.
Finally, we can get back to updating and adding a new example in UsersIntegrationSpec.

We add the following new example:

```scala
"be informed of non-existing user" in new WithChromeBrowser {
  browser goTo "/"
  browser title() mustEqual "Home"
  browser click "#usersButtonGroup"
  browser click "#userSearch"
  browser find "#content" getText() must contain("User Search")

  browser $("#id") text "-1"
  browser click "#search"
  browser find "#content" getText() must contain("No users found for given search criteria")
}
```

My first thought is that the search form will be enhanced with an error message, upon an invalid search criteria.
And correctly we are red, as of course we have no implementation.

To get to green, I'm first going to alter how the form data is submitted.
Currently we have a standard submission (see the form code above) - instead I'm going to submit with some more CoffeeScript.
Why? Because we are coding a single web page application, and we expect JSON (or an error string) from the server, we shall make an Ajax "post", but as mentioned, do it with the following CoffeeScript:

```coffeescript
$ ->
    $("#userSearchForm").submit ->
        $.post($(this).attr("action"), $(this).serialize(), (users) ->
            $("#content").html("<ul id='usersList' style='color: white'></ul>")

            $.each users, (index, user) ->
                $("#usersList").append("<li>#{user.id}, #{user.firstName}, #{user.lastName}</li>")
        )

        false
```

Hopefully you will find this fairly interesting! What is it all about?
The function within the post i.e. after the third "->", is code I've just copied from getting all the users.
Note that the actual "post" involves; where to post to, so we get the "action" from the declared form; serialize all the form field data.
So that just leaves a few others lines to discuss.
The very first "$ ->" is the CoffeeScript version of the jQuery "ready document" i.e. what to do upon form/page load.
Next we attach a function (the post) to the "form submission".
And finally we return "false" from the "form submission" function - "false" stops the continuation of the normal form submission - you could say we are overridding the normal functionality.