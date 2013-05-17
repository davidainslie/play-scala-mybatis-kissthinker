package persistence

import org.mybatis.scala.mapping._
import org.mybatis.scala.mapping.Binding._
import persistence.mybatis.DAO
import models.User

class UserDAO extends DAO {
  configuration ++= Seq(insert, update, findAll, findById)

  def save(user: User): User = user.id match {
    case 0 => inTransaction { implicit session =>
      insert(user)
      user
    }

    case _ => inTransaction { implicit session =>
      update(user)
      user
    }
  }

  def all: List[User] = inTransaction { implicit session =>
    findAll().toList
  }

  def find(id: Long): Option[User] = inTransaction { implicit session =>
    findById(id)
  }

  private def userResultMap = new ResultMap[User] {
    idArg(column = "id", javaType = T[Long])
    arg(column = "first_name", javaType = T[String])
    arg(column = "last_name", javaType = T[String])
  }

  private lazy val insert = new Insert[User] {
    keyGenerator = JdbcGeneratedKey(null, "id")

    def xsql = <xsql>
      insert into users (first_name, last_name)
      values ({"firstName"?}, {"lastName"?})
    </xsql>
  }

  private lazy val update = new Update[User] {
    def xsql = <xsql>
      update users
      set first_name = {"firstName"?}, last_name = {"lastName"?}
      where id = {"id"?}
    </xsql>
  }

  private lazy val findAll = new SelectList[User] {
    resultMap = userResultMap

    def xsql = "select id, first_name, last_name from users"
  }

  private lazy val findById = new SelectOneBy[Long, User] {
    resultMap = userResultMap

    def xsql = <xsql>select * from users where id = {"id"?}</xsql>
  }
}