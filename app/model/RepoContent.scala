package model

import play.api.libs.json.{Json, OFormat}

case class RepoContent(
                      name: String,
                      `type`: String,
                      path: String
                      )

object RepoContent {
  implicit val formats: OFormat[RepoContent] = Json.format[RepoContent]
}