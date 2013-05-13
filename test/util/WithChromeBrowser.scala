package util

import play.api.test.WithBrowser
import org.openqa.selenium.chrome.ChromeDriver

abstract class WithChromeBrowser extends WithBrowser(webDriver = ChromeDriverClass.get)

object ChromeDriverClass {
  /** The new driver avoids having to manually accept terms and conditions, but it can be slow - use/load one of the following two */
  System.setProperty("webdriver.chrome.driver", "chromedriver2.exe")
  // System.setProperty("webdriver.chrome.driver", "chromedriver.exe")

  def get = classOf[ChromeDriver]
}