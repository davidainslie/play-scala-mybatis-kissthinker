package json

import org.specs2.mutable.Specification
import play.api.mvc.Result
import play.api.test.Helpers._

trait JSONMatcher extends Specification {
  implicit def resultToJSONMatcher(result: Result) = new JSONMatcher(result)

  class JSONMatcher(result: Result) {
    def isJSON = {
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      charset(result) must beSome("utf-8")
    }
  }
}