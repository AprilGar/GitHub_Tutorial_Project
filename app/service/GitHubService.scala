package service

import cats.data.EitherT
import connector.GitHubConnector
import model.{APIError, User}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GitHubService @Inject()(connector: GitHubConnector) {

  def getGithubUser(urlOverride: Option[String] = None, username: String)(implicit ec: ExecutionContext):
  EitherT[Future, APIError, User] =
    connector.get[User](urlOverride.getOrElse(s"https://api.github.com/users/$username"))

}
