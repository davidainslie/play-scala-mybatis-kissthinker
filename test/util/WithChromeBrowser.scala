package util

import play.api.test.WithBrowser
import org.openqa.selenium.chrome.ChromeDriver

abstract class WithChromeBrowser extends WithBrowser(webDriver = ChromeDriverClass.get)

object ChromeDriverClass {
  /* System.setProperty("webdriver.chrome.driver", "chromedriver2.exe") The new driver avoids having to manually accept terms and conditions, but it is slow */
  System.setProperty("webdriver.chrome.driver", "chromedriver.exe")

  def get = classOf[ChromeDriver]
}