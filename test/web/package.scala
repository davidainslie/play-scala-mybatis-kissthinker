import org.openqa.selenium.chrome.ChromeDriver
import play.api.test.WithBrowser

package object web {
  /** The new driver avoids having to manually accept terms and conditions, but it can be slow - use/load one of the following two */
  val os = System.getProperty("os.name").toLowerCase

  if (os.indexOf("mac") >= 0) {
    System.setProperty("webdriver.chrome.driver", "chromedriver2")
  } else if (os.indexOf("win") >= 0) {
    System.setProperty("webdriver.chrome.driver", "chromedriver.exe")
  }

  def chromeDriverClass = classOf[ChromeDriver]

  abstract class WithChromeBrowser extends WithBrowser(webDriver = chromeDriverClass)
}