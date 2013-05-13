package persistence

import org.mybatis.scala.mapping._
import org.mybatis.scala.mapping.Binding._
import persistence.mybatis.DAO
import models.User

class UserDAO extends DAO {
  configuration ++= Seq(insert, update, findAll, findById)

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

  def all: List[User] = inTransaction { implicit session =>
    findAll().map(u => User(u.id, u.first_name, u.last_name)).toList
  }

  private lazy val insert = new Insert[UserEntry] {
    keyGenerator = JdbcGeneratedKey(null, "id")

    def xsql = <xsql>
      insert into users (first_name, last_name)
      values ({"first_name"?}, {"last_name"?})
    </xsql>
  }

  private lazy val update = new Update[User] {
    def xsql = <xsql>
      update users
      set first_name = {"firstName"?}, last_name = {"lastName"?}
      where id = {"id"?}
    </xsql>
  }

  private lazy val findAll = new SelectList[UserEntry] {
    def xsql = "select id, first_name, last_name from users"
  }

  private lazy val findById = new SelectOneBy[Long, UserEntry] {
    def xsql = <xsql>select * from users where id = {"id"?}</xsql>
  }
}

class UserEntry(user: User) {
  def this() = this(User(0, "", ""))

  var id : Long = user.id

  var first_name : String = user.firstName

  var last_name : String = user.lastName

  override def toString() = s"UserEntry [id: $id, firstName: $first_name, lastName: $last_name]"
}