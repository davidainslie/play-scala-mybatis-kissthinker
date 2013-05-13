package persistence

import org.mybatis.scala.mapping.{Update, JdbcGeneratedKey, SelectOneBy, Insert}
import org.mybatis.scala.mapping.Binding._
import models.User
import persistence.mybatis.DAO

class UserDAO extends DAO {
  configuration ++= Seq(insert, update, findById)

  def save(user: User): User = user.id match {
    case 0 => inTransaction { implicit session =>
      val userEntry = new UserEntry(user)
      insert(userEntry)
      user.copy(id = userEntry.id)
    }

    case _ => inTransaction { implicit session =>
      update(user)
      user
    }
  }

  private lazy val insert = new Insert[UserEntry] {
    keyGenerator = JdbcGeneratedKey(null, "id")

    def xsql = <xsql>
      insert into user (first_name, last_name)
      values ({"first_name"?}, {"last_name"?})
    </xsql>
  }

  private lazy val update = new Update[User] {
    def xsql = <xsql>
      update user
      set first_name = {"firstName"?}, last_name = {"lastName"?}
      where id = {"id"?}
    </xsql>
  }

  private lazy val findById = new SelectOneBy[Long, UserEntry] {
    def xsql = <xsql>select * from user where id = {"id"?}</xsql>
  }

  class UserEntry(user: User) {
    var id : Long = user.id

    var first_name : String = user.firstName

    var last_name : String = user.lastName

    override def toString() = s"UserEntry [id: $id, firstName: $first_name, lastName: $last_name]"
  }
}