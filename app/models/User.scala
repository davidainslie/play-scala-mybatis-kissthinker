package models

case class User(id: Long = 0, firstName: String = "", lastName: String = "")

object User {
  import play.api.libs.json.Json

  implicit val format = Json.format[User]
}