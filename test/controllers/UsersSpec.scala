package controllers

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.http.HeaderNames
import play.api.test.{WithServer, FakeRequest}
import models.User

class UsersSpec extends Specification {
  "User" should {
    "view a user profile" in {
      val request = FakeRequest().withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      val result = Users.view(1)(request)

      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      charset(result) must beSome("utf-8")

      import play.api.libs.json.Json
      implicit val userFormat = Json.format[User]

      val user = Json.parse(contentAsString(result)).as[User]

      user.id mustEqual 1
    }

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
  }
}