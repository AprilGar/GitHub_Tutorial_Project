package connector

import model.{APIError, FileContent, RepoContent, Repos, User}
import play.api.libs.json.{JsError, JsSuccess, OFormat}
import play.api.libs.ws.WSClient

import java.util.Base64
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GitHubConnector @Inject()(ws: WSClient) {

  def get[Response](url: String)(implicit rds: OFormat[Response], ec: ExecutionContext): Future[Either[APIError, User]] = {
    val request = ws.url(url)
    val response = request.get()
      response.map {
        result =>
          result.json.validate[User] match {
            case JsSuccess(returnedUser, _) => Right(User(returnedUser.login,
              returnedUser.created_at,
              returnedUser.location,
              returnedUser.followers,
              returnedUser.following))
            case JsError(errors) => Left(APIError.BadAPIResponse(500, "Not found"))
          }
      }
    }

  def getRepos[Response](url: String)(implicit rds: OFormat[Response], ec: ExecutionContext): Future[Either[APIError, Seq[Repos]]] = {
    val request = ws.url(url)
    val response = request.get()
    response.map {
      result =>
        result.json.validate[Seq[Repos]] match {
          case JsSuccess(returnedUserRepos, _) => Right(returnedUserRepos.map{ repo =>
            Repos(
              repo.name,
              repo.description,
              repo.`private`,
              repo.owner,
              repo.watchers_count,
              repo.forks_count
            )
          })
          case JsError(errors) => Left(APIError.BadAPIResponse(500, "Not found"))
        }
    }
  }

  def getRepoInfo[Response](url: String)(implicit rds: OFormat[Response], ec: ExecutionContext): Future[Either[APIError, Seq[RepoContent]]] = {
    val request = ws.url(url)
    val response = request.get()
    response.map {
      result =>
        result.json.validate[Seq[RepoContent]] match {
          case JsSuccess(returnedRepoContent, _) => Right(returnedRepoContent.map { repoContent =>
            RepoContent(repoContent.name, repoContent.`type`, repoContent.path)
          })
          case JsError(errors) => Left(APIError.BadAPIResponse(500, "Not found"))
        }
    }}

  def getFileContent [Response](url: String)(implicit rds: OFormat[Response], ec: ExecutionContext) = {
    val request = ws.url(url)
    val response = request.get()
    response.map {
      result =>
      result.json.validate[FileContent] match {
        case JsSuccess(returnedFileContent, _) =>
          val decodedBase64 = Base64.getMimeDecoder().decode(returnedFileContent.content).map(_.toChar).mkString
            Right(FileContent(returnedFileContent.name, decodedBase64))
        case JsError(errors) => Left(APIError.BadAPIResponse(500, "Not found"))
      }
    }
  }

  def getFolderContent[Response](url: String)(implicit rds: OFormat[Response], ec: ExecutionContext): Future[Either[APIError, Seq[RepoContent]]] = {
    val request = ws.url(url)
    val response = request.get()
    response.map {
      result =>
        result.json.validate[Seq[RepoContent]] match {
          case JsSuccess(returnedRepoContent, _) => Right(returnedRepoContent.map { repoContent =>
            RepoContent(repoContent.name, repoContent.`type`, repoContent.path)
          })
          case JsError(errors) => Left(APIError.BadAPIResponse(500, "Not found"))
        }
    }
  }


}
