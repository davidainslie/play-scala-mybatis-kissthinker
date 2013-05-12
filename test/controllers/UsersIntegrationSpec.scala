package controllers

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.http.HeaderNames
import play.api.test.{WithBrowser, FakeRequest}
import models.User

class UsersIntegrationSpec extends Specification {
  "User" should {
    "view a user profile" in new WithBrowser {
      browser.goTo("/users/1")
      //browser.title() mustEqual "User"
      pending
    }
  }
}