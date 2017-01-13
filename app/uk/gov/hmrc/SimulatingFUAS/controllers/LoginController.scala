package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.Play.current
import play.api.data.Form
import uk.gov.hmrc.SimulatingFUAS.models.{AuthorisingAuth, Forms, User}

import scala.concurrent.Future


object LoginController extends Controller with FrontendController {

  val userLoginForm: Form[User] = Forms.userLoginForm

  def loginPage(continueUrl: String): Action[AnyContent] = Action.async {
    implicit request => {
      Future.successful(Ok(uk.gov.hmrc.SimulatingFUAS.views.html.loginIndex(userLoginForm, request.uri)(request, applicationMessages)))
    }
  }

  val about: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(uk.gov.hmrc.SimulatingFUAS.views.html.about()(request, applicationMessages)).withHeaders())
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
    val auth:AuthorisingAuth = new AuthorisingAuth
    if (auth.checkName(name: String, password: String)) {
      run
    } else {
      val formWithError = userLoginForm.withGlobalError("Please check and re-enter your user name and password")
      Ok(uk.gov.hmrc.SimulatingFUAS.views.html.loginIndex(formWithError, continueUrl)(request, applicationMessages))
    }
  }
}
