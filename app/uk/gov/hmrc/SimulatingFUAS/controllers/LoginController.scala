package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.Logger
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.Play.current
import play.api.data.Form
import uk.gov.hmrc.SimulatingFUAS.models.{AuthorisingAuth, Forms, User}
import uk.gov.hmrc.SimulatingFUAS.views.html._

import scala.concurrent.Future


object LoginController extends Controller with FrontendController {

  val auth:AuthorisingAuth = new AuthorisingAuth
  val userLoginForm: Form[User] = Forms.userLoginForm

  implicit val anyContentBodyParser: BodyParser[AnyContent] = parse.anyContent

  def release: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(release_note()))
  }

  def loginPage(continueUrl: String): Action[AnyContent] = Action.async {
    implicit request => {
      Future.successful(Ok(loginIndex(userLoginForm, continueUrl)(request, applicationMessages)))
    }
  }

  def about: Action[AnyContent] = securedAction[AnyContent] {
    implicit request =>
      Future.successful(Ok(about_app()(request, applicationMessages)).withHeaders())
  }

  def check(continueUrl: String) = Action {
    implicit request =>

      val loginForm = userLoginForm.bindFromRequest()
      val name = userLoginForm.bindFromRequest().data("name")
      val password = userLoginForm.bindFromRequest().data("password")

      checkAuth(name, password, loginForm, continueUrl) {
        Redirect(continueUrl).withSession("userName" -> name)
      }
  }

  def checkAuth(name: String, password: String, loginForm: Form[User], continueUrl: String)
               (run: => Result)(implicit request: Request[AnyContent]): Result = {
    if (auth.checkName(name: String, password: String)) run
    else {
      val formWithError = userLoginForm.withGlobalError("Please check and re-enter your user name and password")
      Ok(loginIndex(formWithError, continueUrl)(request, applicationMessages))
    }
  }

  def securedAction[T](furtherAction: Request[T] => Future[Result])(implicit bodyParser: BodyParser[T]): Action[T] =
    Action.async(bodyParser) { implicit request =>
      hasValidToken{furtherAction(request)}{
        request.session.get("userName").map { userName =>
          Logger.info(s"Request ${request.method} ${request.uri} done by: $userName")
          furtherAction(request)
        }.getOrElse(
            Future.successful(Redirect(routes.LoginController.loginPage(request.uri)))
        )
      }
    }

  private def hasValidToken(furtherActionWithToken: => Future[Result])(furtherActionWithoutToken: => Future[Result])
                           (implicit request: RequestHeader): Future[Result] = {
    if (auth.checkToken) furtherActionWithToken
    else furtherActionWithoutToken
  }
}
