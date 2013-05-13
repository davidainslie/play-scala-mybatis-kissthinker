package controllers

import play.api.mvc._
import play.api.libs.json.Json._
import models.User
import play.api.libs.json.Json

object Users extends Controller {
  def view = Action {
    implicit val userFormat = format[User]

    val users = User(1) :: User(2) :: User(3) :: Nil
    Ok(toJson(users))
  }

  def view(id: Long) = Action {
    implicit val userFormat = format[User]

    val user = User(1)
    Ok(toJson(user))
  }
}