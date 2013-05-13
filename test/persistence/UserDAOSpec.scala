package persistence

import org.specs2.mutable.Specification
import models.User
import play.api.test.WithServer

class UserDAOSpec extends Specification {
  "UserDAO" should {
    "insert new User which will be assigned next available ID, in this case 1" in new WithServer {
      val user = UserDAO.save(User(firstName = "Scooby", lastName = "Doo"))
      user.id mustEqual 1
    }
  }
}