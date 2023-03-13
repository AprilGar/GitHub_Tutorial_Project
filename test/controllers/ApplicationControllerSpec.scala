package controllers

import baseSpec.BaseSpecWithApplication
import model.User
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Result}
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import play.api.test._

import scala.concurrent.Future

class ApplicationControllerSpec extends BaseSpecWithApplication with Injecting {

  val TestApplicationController = new ApplicationController(component, service, executionContext, repository)

  private val user: User = User(
    "login1",
    "06/02/23",
    Some("location"),
    2,
    1
  )

  private val user2: User = User(
    "login1",
    "06/02/23",
    Some("location"),
    4,
    1
  )

  override def beforeEach(): Unit = repository.deleteAll()
  override def afterEach(): Unit = repository.deleteAll()

  "ApplicationController .create()" should {
    "create a user in the database" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(user))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult) shouldBe Status.CREATED
      afterEach()
    }

    "give an bad request error if it cannot create a user in the database" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.obj())
      val createdResult = TestApplicationController.create()(request)
      status(createdResult) shouldBe Status.INTERNAL_SERVER_ERROR
      afterEach()
    }
  }

  "ApplicationController .read()" should {
    "find a user in the database by login" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(user))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult) shouldBe Status.CREATED

      val readRequest: FakeRequest[AnyContent] = buildGet("/read/login1")
      val readResult: Future[Result] = TestApplicationController.read("login1")(readRequest)
      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[User] shouldBe user
      afterEach()
    }

    "throw an error if login cannot be found" in {
      beforeEach()
      val readRequest: FakeRequest[AnyContent] = buildGet("/read/login1")
      val readResult: Future[Result] = TestApplicationController.read("login1")(readRequest)
      status(readResult) shouldBe Status.INTERNAL_SERVER_ERROR
      afterEach()
    }
  }

  "ApplicationController .update()" should {

    "update a user information using the login" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(user))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = buildPut("/update/login1").withBody[JsValue](Json.toJson(user2))
      val updateResult: Future[Result] = TestApplicationController.update("login1")(updateRequest)
      status(updateResult) shouldBe Status.ACCEPTED
      afterEach()
    }

    "give an bad request error if it cannot update a user" in {
      beforeEach()
      val updateRequest: FakeRequest[JsValue] = buildPut("/update/login1").withBody[JsValue](Json.obj())
      val updateResult: Future[Result] = TestApplicationController.update("login1")(updateRequest)
      status(updateResult) shouldBe Status.BAD_REQUEST
      afterEach()
    }

  }

  "ApplicationController .delete()" should {
    "delete a user using the login" in {
      beforeEach()

      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(user))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult) shouldBe Status.CREATED

      val deleteRequest: FakeRequest[AnyContentAsEmpty.type] = buildDelete("delete/login1")
      val deleteResponse: Future[Result] = TestApplicationController.delete("login1")(deleteRequest)
      status(deleteResponse) shouldBe Status.ACCEPTED

      afterEach()
    }

    "not delete a user if login cannot be found" in {
      val deleteRequest: FakeRequest[AnyContentAsEmpty.type] = buildDelete("delete/login4")
      val deleteResponse: Future[Result] = TestApplicationController.delete("login4")(deleteRequest)
      status(deleteResponse) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "ApplicationController .getRepositories" should {

//    "display a list of user repositories" in {
//
//    }

    "not display a list of user repositories if user cannot be found" in {
      val getRepoRequest = buildGet("/github/users/login4/repositories")
      val getRepoResponse = TestApplicationController.getRepositories("login4")(getRepoRequest)
      status(getRepoResponse) shouldBe Status.INTERNAL_SERVER_ERROR
    }

  }


}
