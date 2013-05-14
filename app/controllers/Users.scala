package controllers

import play.api.mvc._
import play.api.libs.json.Json._
import models.User
import play.api.libs.json.Json
import persistence.UserDAO

object Users extends Controller {
  val userDAO = new UserDAO()

  def view = Action {
    implicit val userFormat = format[User]

    val users = userDAO.all
    Ok(toJson(users))
  }

  def view(id: Long) = Action {
    implicit val userFormat = format[User]

    val user = User(1)
    Ok(toJson(user))
  }
}