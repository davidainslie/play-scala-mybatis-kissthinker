package controllers

import org.specs2.mutable.Specification
import util.WithChromeBrowser

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