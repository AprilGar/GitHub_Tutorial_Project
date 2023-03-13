package controllers

import model.{APIError, User, Repos}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import repository.DataRepoTrait
import service.GitHubService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationController @Inject()(val controllerComponents: ControllerComponents, val gitHubService: GitHubService, implicit val ec: ExecutionContext, val dataRepository: DataRepoTrait) extends BaseController {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[User] match {
      case JsSuccess(user, _) => dataRepository.create(user).map(_ => Created)
      case JsError(_) => Future(InternalServerError)
    }
  }

  def read(login: String): Action[AnyContent] = Action.async { implicit request =>
    dataRepository.read(login).map {
      case Right(user) => Ok(Json.toJson(user))
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def update(login: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[User]
    match {
      case JsSuccess(user, _) =>
        dataRepository.update(login, user).map(updatedUser => Accepted)
      case JsError(_) => Future(BadRequest)
    }
  }

  def delete(login: String): Action[AnyContent] = Action.async { implicit request =>
    dataRepository.delete(login).map {
      case Right(deletedUser: String) => Accepted
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def addGithubUserToDb(login: String):Action[AnyContent] = Action.async { implicit request =>
    gitHubService.addUserToDb(login = login).map{
        case Left(error: APIError) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
        case Right(user: User) => Created(views.html.findUser(user))
      }
    }

  def getGithubUser(login: String): Action[AnyContent] = Action.async { implicit request =>
    gitHubService.getGithubUser(login = login).map {
      case Right(user) => Ok(views.html.findUser(user))
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def getRepositories(login: String): Action[AnyContent] = Action.async { implicit request =>
    gitHubService.getUserRepositories(login = login).map {
      case Right(repos) => Ok(views.html.displayRepos(repos))
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def getRepoInfo(login: String, repoName: String): Action[AnyContent] = Action.async { implicit request =>
    gitHubService.getRepoContent(login = login, repoName = repoName).map {
      case Right(content) => Ok(views.html.repoContent(content, repoName, login))
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def fileContent(login: String, repoName: String, path: String): Action[AnyContent] = Action.async { implicit request =>
    gitHubService.getFileContent(login = login, repoName = repoName, path = path).map {
      case Right(content) => Ok(views.html.displayFileContent(content))
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def folderContent(login: String, repoName: String, path: String): Action[AnyContent] = Action.async { implicit request =>
    gitHubService.getFolderContent(login = login, repoName = repoName, path = path).map {
      case Right(content) => Ok(views.html.repoContent(content, repoName, login))
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

}
