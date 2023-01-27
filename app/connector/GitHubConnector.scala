package connector

import cats.data.{EitherT, RWS}
import model.{APIError, User}
import play.api.libs.json.{JsError, JsSuccess, OFormat}
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GitHubConnector @Inject()(ws: WSClient) {
  def get[Response](url: String)(implicit rds: OFormat[Response], ec: ExecutionContext): EitherT[Future, APIError, User] = {
    val request = ws.url(url)
    val response = request.get()
    EitherT {
      response.map {
        result =>
          result.json.validate[User] match {
            case JsSuccess(returnedUser, _) => Right(User(returnedUser.username,
              returnedUser.location,
              returnedUser.dateAccountCreated,
              returnedUser.numberOfFollowers,
              returnedUser.numberFollowing))
            case JsError(errors) => Left(APIError.BadAPIResponse(500, "Not found"))
          }
      }
    }
  }
}
