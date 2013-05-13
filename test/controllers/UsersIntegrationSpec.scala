package controllers

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.http.HeaderNames
import play.api.test._
import models.User
import play.api.test.TestServer
import play.api.test.FakeApplication
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxProfile}
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver

class UsersIntegrationSpec extends Specification {
  System.setProperty("webdriver.chrome.driver", "C:/git/play-scala-mybatis-kissthinker/chromedriver.exe")

  "User" should {
    "view a user profile" in new WithBrowser(webDriver = classOf[ChromeDriver]) {
      browser.goTo("/")
      browser.click("#user1")
      browser.find("#content") contains "User ID: 1"
    }
  }
}