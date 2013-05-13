package persistence

import play.api.Play.current
import play.api.db.DB.getDataSource
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory
import org.mybatis.scala.config.{Configuration, Environment}
import org.mybatis.scala.session.Session

object MyBatis {
  private val configuration = Configuration(Environment("default", new ManagedTransactionFactory(), getDataSource()))

  configuration ++= UserDAO

  private val sessionManager = configuration.createPersistenceContext

  def inTransaction[R](f: Session => R): R = sessionManager.transaction(f)
}