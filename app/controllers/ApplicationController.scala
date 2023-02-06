package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import service.GitHubService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ApplicationController @Inject()(val controllerComponents: ControllerComponents, val gitHubService: GitHubService, implicit val ec: ExecutionContext) extends BaseController {

  def getGithubUser(username: String): Action[AnyContent] = Action.async { implicit request =>
    gitHubService.getGithubUser(username = username).value.map {
      case Right(user) => Ok(Json.toJson(user))
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

}
