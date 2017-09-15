package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.Logger
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import uk.gov.hmrc.SimulatingFUAS.models.{AuthorisingAuth, Forms, User}
import uk.gov.hmrc.SimulatingFUAS.views.html._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object LoginController extends Controller with FrontendController {

  val auth: AuthorisingAuth = new AuthorisingAuth
  val userLoginForm: Form[User] = Forms.userLoginForm

  implicit val anyContentBodyParser: BodyParser[AnyContent] = parse.anyContent

  def loginPage(continueUrl: Option[String]): Action[AnyContent] = Action.async {
    implicit request => {
      Future.successful(Ok(loginIndex(userLoginForm, continueUrl.getOrElse("/fuaas-simulator/"))))
    }
  }

  def about: Action[AnyContent] = securedAction[AnyContent] {
    implicit request =>
      Future.successful(Ok(about_app()).withHeaders())
  }

  def check(continueUrl: String) = Action.async {
    implicit request =>

      val loginForm = userLoginForm.bindFromRequest()

      checkAuth(loginForm, continueUrl) {
        Future.successful(Redirect(continueUrl).withSession("userName" -> loginForm.data("name")))
      }
  }

  def checkAuth(loginForm: Form[User], continueUrl: String)
               (run: => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {

    val name = loginForm.data("name")
    val password = loginForm.data("password")

    if (auth.checkName(name: String, password: String)) run
    else {
      val formWithError = userLoginForm.withGlobalError("Please check and re-enter your user name and password")
      Future.successful(Ok(loginIndex(formWithError, continueUrl)))
    }
  }

  def securedAction[T](furtherAction: Request[T] => Future[Result])(implicit bodyParser: BodyParser[T]): Action[T] =
    Action.async(bodyParser) { implicit request =>
      hasValidToken {
        furtherAction(request)
      } {
        request.session.get("userName").map { userName =>
          Logger.info(s"Request ${request.method} ${request.uri} done by: $userName")
          furtherAction(request)
        }.getOrElse(
          Future.successful(Redirect(routes.LoginController.loginPage(Some(request.uri))))
        )
      }
    }

  private def hasValidToken(furtherActionWithToken: => Future[Result])(furtherActionWithoutToken: => Future[Result])
                           (implicit request: RequestHeader): Future[Result] = {
    if (auth.checkToken) furtherActionWithToken
    else furtherActionWithoutToken
  }
}
