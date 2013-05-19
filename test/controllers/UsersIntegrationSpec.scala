package controllers

import org.specs2.mutable.Specification
import java.util.concurrent.TimeUnit
import web.WithChromeBrowser

class UsersIntegrationSpec extends Specification {
  "User" should {
    "view a user profile" in new WithChromeBrowser {
      browser goTo "/"
      browser title() mustEqual "Home"
      browser click "#usersButtonGroup"
      browser click "#userSearch"
      browser find "#content" getText() must contain("User Search")

      browser $("#id") text "1"
      browser click "#search"
      browser find "#content" getText() must contain("Paul McCartney")
    }

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
  }
}