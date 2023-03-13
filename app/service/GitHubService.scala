package service

import connector.GitHubConnector
import model.{APIError, FileContent, RepoContent, Repos, User}
import repository.DataRepoTrait

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GitHubService @Inject()(connector: GitHubConnector, val dataRepository: DataRepoTrait) (implicit ec: ExecutionContext) {

  def getGithubUser(urlOverride: Option[String] = None, login: String): Future[Either[APIError, User]] =
    connector.get[User](urlOverride.getOrElse(s"https://api.github.com/users/$login"))

  def getUserRepositories(urlOverride: Option[String] = None, login: String): Future[Either[APIError, Seq[Repos]]] =
    connector.getRepos[Repos](urlOverride.getOrElse(s"https://api.github.com/users/$login/repos"))

  def getRepoContent(urlOverride: Option[String] = None, login: String, repoName: String): Future[Either[APIError, Seq[RepoContent]]] =
    connector.getRepoInfo[RepoContent](urlOverride.getOrElse(s"https://api.github.com/repos/$login/$repoName/contents"))

  def addUserToDb(login: String):Future[Either[APIError, User]]  = {
    getGithubUser(login = login).flatMap{
      case Right(user) => dataRepository.create(user)
      case Left(_) => Future(Left(APIError.BadAPIResponse(400, "User cannot be found")))
    }
  }

  def getFileContent(urlOverride: Option[String] = None, login: String, repoName: String, path: String): Future[Either[APIError, FileContent]] =
    connector.getFileContent[FileContent](urlOverride.getOrElse(s"https://api.github.com/repos/$login/$repoName/contents/$path"))

  def getFolderContent(urlOverride: Option[String] = None, login: String, repoName: String, path: String): Future[Either[APIError, Seq[RepoContent]]] =
    connector.getFolderContent[RepoContent](urlOverride.getOrElse(s"https://api.github.com/repos/$login/$repoName/contents/$path"))
}
