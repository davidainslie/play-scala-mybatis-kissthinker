package controllers

import play.api.mvc._
import play.api.libs.json.Json._
import models.User
import play.api.libs.json.Json
import persistence.UserDAO

object Users extends Controller with User.JSON {
  def search = Action {
    Ok(views.html.user.search())
  }

  def users = Action {
    val users = new UserDAO().all
    Ok(toJson(users))
  }

  def user(id: Long) = Action {
    new UserDAO().find(id) match {
      case Some(u: User) => Ok(toJson(u))
      case _ => NotFound
    }
  }
}