package controllers

import org.specs2.mutable.Specification
import util.WithChromeBrowser
import java.util.concurrent.TimeUnit

class UsersIntegrationSpec extends Specification {
  "User" should {
    "view a user profile" in new WithChromeBrowser {
      browser.goTo("/")
      browser.title() mustEqual "Home"
      browser.click("#usersButtonGroup")
      browser.click("#user1")
      browser.find("#content") contains "User ID: 1"
    }

    "view all users" in new WithChromeBrowser {
      browser.goTo("/")
      browser.title() mustEqual "Home"
      browser.click("#usersButtonGroup")
      browser.click("#users")

      browser.waitUntil[Boolean](3, TimeUnit.SECONDS) {
        browser.pageSource contains "Lennon"
      }

      browser.find("#usersList").getText contains "2, John, Lennon"
    }
  }
}