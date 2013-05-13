package persistence

import org.specs2.mutable.Specification
import models.User
import play.api.test.WithServer

class UserDAOSpec extends Specification {
  "UserDAO" should {
    "insert new User which will be assigned next available ID, in this case 1" in new WithServer {
      val user = new UserDAO().save(User(firstName = "Scooby", lastName = "Doo"))
      user.id mustEqual 1
    }

    "insert new User which will be assigned next available ID, in this case 1 to double check bootstrap/teardown" in new WithServer {
      val user = new UserDAO().save(User(firstName = "Scooby", lastName = "Doo"))
      user.id mustEqual 1
    }

    "find all User" in new WithServer {
      new UserDAO().save(User(firstName = "Paul", lastName = "McCartney"))
      new UserDAO().save(User(firstName = "John", lastName = "Lennon"))
      new UserDAO().save(User(firstName = "George", lastName = "Harrison"))
      new UserDAO().save(User(firstName = "Ringo", lastName = "Starr"))

      val users = new UserDAO().all
      users must contain(User(3, "George", "Harrison"),
                         User(4, "Ringo", "Starr"),
                         User(2, "John", "Lennon"),
                         User(1, "Paul", "McCartney")).only
    }
  }
}