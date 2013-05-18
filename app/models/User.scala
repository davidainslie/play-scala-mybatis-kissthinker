package models

case class User(id: Long = 0, firstName: String = "", lastName: String = "")

object User {
  trait JSON {
    import play.api.libs.json.Json

    implicit val format = Json.format[User]

    def parse(json: String) = Json.parse(json)
  }
}