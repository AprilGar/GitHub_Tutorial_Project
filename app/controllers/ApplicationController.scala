package controllers

import model.User
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import repository.DataRepoTrait
import service.GitHubService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationController @Inject()(val controllerComponents: ControllerComponents, val gitHubService: GitHubService, implicit val ec: ExecutionContext, val dataRepository: DataRepoTrait) extends BaseController {

  def getGithubUser(username: String): Action[AnyContent] = Action.async { implicit request =>
    gitHubService.getGithubUser(username = username).value.map {
      case Right(user) => Ok(Json.toJson(user))
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

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

}
