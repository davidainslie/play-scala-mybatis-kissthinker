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
      browser.await().atMost(10, TimeUnit.SECONDS).until("#content").containsText("User Search")

      browser $("#id") text "1"
      browser submit "#userSearchForm"

      browser.waitUntil[Boolean](10, TimeUnit.SECONDS) {
        browser title() contains "Users"
      }

      browser find "#content" getText() must contain("1, Paul, McCartney")
    }

    "be informed of non-existing user" in new WithChromeBrowser {
      browser goTo "/"
      browser title() mustEqual "Home"
      browser click "#usersButtonGroup"
      browser click "#userSearch"
      browser.await().atMost(10, TimeUnit.SECONDS).until("#content").containsText("User Search")

      browser $("#id") text "-1"
      browser click "#search"

      browser.waitUntil[Boolean](10, TimeUnit.SECONDS) {
        browser title() contains "Users"
      }

      browser find "#content" getText() must contain("No users found for given search criteria")
    }

    "view all users" in new WithChromeBrowser {
      browser goTo "/"
      browser title() mustEqual "Home"
      browser click "#usersButtonGroup"
      browser click "#users"

      browser.waitUntil[Boolean](5, TimeUnit.SECONDS) {
        browser pageSource() contains "Lennon"
      }

      browser find "#usersList" getText() must contain("2, John, Lennon")
    }
  }
}