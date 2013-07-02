package controllers

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.http.HeaderNames
import play.api.test.{WithServer, FakeRequest}
import models.User
import models.User._
import json.JSONMatcher
import play.api.libs.json.Json

class UsersSpec extends Specification with JSONMatcher {
  "User" should {
    "view a user profile" in new WithServer {
      val request = FakeRequest().withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      val result = Users.user(1)(request)
      result isJSON

      val user = Json.parse(contentAsString(result)).as[User]

      user mustEqual User(1, "Paul", "McCartney")
    }

    "get an error for a non existing user request" in new WithServer {
      val request = FakeRequest().withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      val result = Users.user(-1)(request)

      status(result) mustEqual NOT_FOUND
    }

    "view all users" in new WithServer {
      val request = FakeRequest().withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      val result = Users.users()(request)
      result isJSON

      val users = Json.parse(contentAsString(result)).as[List[User]]

      users.size mustEqual 4
    }

    "view all JSON users" in new WithServer {
      val request = FakeRequest().withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      val result = Users.users()(request)
      result isJSON

      val users = contentAsString(result)

      users must */("id" -> 2)
      users must */("firstName" -> "John")
      users must */("lastName" -> "Lennon")
    }
  }
}