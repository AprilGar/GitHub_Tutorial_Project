package model

import play.api.libs.json.{Json, OFormat}

case class Repos(
                name: String,
                description: Option[String],
                `private`: Boolean,
                owner: Owner,
                watchers_count: Int,
                forks_count: Int
                )

case class Owner (login: String)

object Repos {
  implicit val formats: OFormat[Repos] = Json.format[Repos]
}

object Owner {
  implicit val formats: OFormat[Owner] = Json.format[Owner]
}