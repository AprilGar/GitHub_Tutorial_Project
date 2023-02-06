package repository

import com.google.inject.ImplementedBy
import model.{APIError, User}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DataRepository])
trait DataRepoTrait {
  def create(user: User): Future[Either[APIError, User]]
  def read(login: String): Future[Either[APIError, User]]
  def update(login: String, user: User): Future[Either[APIError.BadAPIResponse, User]]
  def delete(login: String): Future[Either[APIError, String]]
}

@Singleton
class DataRepository @Inject()(
                                mongoComponent: MongoComponent
                              )(implicit ec: ExecutionContext) extends PlayMongoRepository[User](
  collectionName = "users",
  mongoComponent = mongoComponent,
  domainFormat = User.formats,
  indexes = Seq(IndexModel(
    Indexes.ascending("login"), new IndexOptions().unique(true)
  )),
  replaceIndexes = false
) with DataRepoTrait {

  def create(user: User): Future[Either[APIError, User]] =
    collection.insertOne(user).toFutureOption()
      .map {
        case Some(result) if result.wasAcknowledged() => Right(user)
        case _ => Left(APIError.BadAPIResponse(424, "User cannot be created"))
      }

  private def byLogin(login: String): Bson =
    Filters.and(
      Filters.equal("login", login)
    )

  def read(login: String): Future[Either[APIError, User]] =
    collection.find(byLogin(login)).headOption flatMap {
      case Some(data) => Future(Right(data))
      case _ => Future(Left(APIError.BadAPIResponse(400, "User cannot be read")))
    }


  def update(login: String, user: User): Future[Either[APIError.BadAPIResponse, User]] =
    collection.replaceOne(
      filter = byLogin(login),
      replacement = user,
      options = new ReplaceOptions().upsert(true)
    ).toFutureOption() map {
      case Some(result) if result.wasAcknowledged() => Right(user)
      case _ => Left(APIError.BadAPIResponse(400, "User cannot be updated"))
    }

  def delete(login: String): Future[Either[APIError, String]] =
    collection.deleteOne(filter = byLogin(login)).toFutureOption().map {
      case Some(result) if result.getDeletedCount == 1 => Right("user deleted")
      case _ => Left(APIError.BadAPIResponse(400, "User cannot be deleted"))
    }

}
