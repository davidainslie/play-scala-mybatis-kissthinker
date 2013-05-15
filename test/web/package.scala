import org.openqa.selenium.chrome.ChromeDriver
import play.api.test.WithBrowser

package object web {
  /** The new driver avoids having to manually accept terms and conditions, but it can be slow - use/load one of the following two */
  System.setProperty("webdriver.chrome.driver", "chromedriver2.exe")
  // System.setProperty("webdriver.chrome.driver", "chromedriver.exe")

  def chromeDriverClass = classOf[ChromeDriver]

  abstract class WithChromeBrowser extends WithBrowser(webDriver = chromeDriverClass)
}