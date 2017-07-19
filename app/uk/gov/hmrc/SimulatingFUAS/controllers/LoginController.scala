package uk.gov.hmrc.SimulatingFUAS.controllers

import com.github.nscala_time.time.Imports._
import play.api.Logger
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import uk.gov.hmrc.SimulatingFUAS.models.{AuthorisingAuth, Forms, ReleaseNote, User}
import uk.gov.hmrc.SimulatingFUAS.views.html._
import uk.gov.hmrc.SimulatingFUAS.views.html.release_views._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future
import scala.io.Source

case class RepoDetails(repoName: String, serviceName: String, version: String, deployDate: DateTime, link: String)

object LoginController extends Controller with FrontendController {

  val auth: AuthorisingAuth = new AuthorisingAuth
  val userLoginForm: Form[User] = Forms.userLoginForm
  val releaseForm: Form[ReleaseNote] = Forms.releaseForm

  implicit val anyContentBodyParser: BodyParser[AnyContent] = parse.anyContent

  val serviceDetailsList: Seq[RepoDetails] = Seq(
    RepoDetails("agent-fi-agent-frontend", "Agents", "1.0.0", new DateTime("2017-07-09"), "https://raw.githubusercontent.com/hmrc/agent-fi-agent-frontend/master/README.md"),
    RepoDetails("agent-fi-agent-frontend", "Agents", "0.9.0", new DateTime("2017-07-1"), "https://raw.githubusercontent.com/hmrc/agent-fi-agent-frontend/master/README.md"),
    RepoDetails("agent-fi-agent-frontend", "Agents", "0.8.0", new DateTime("2017-06-09"), "https://raw.githubusercontent.com/hmrc/agent-fi-agent-frontend/master/README.md"),
    RepoDetails("agent-fi-agent-frontend", "Agents", "0.7.0", new DateTime("2017-05-19"), "https://raw.githubusercontent.com/hmrc/agent-fi-agent-frontend/master/README.md"),
    RepoDetails("fhdds-frontend", "FHDDS", "0.6.0", new DateTime("2017-07-14"), "https://raw.githubusercontent.com/hmrc/fhdds-frontend/master/README.md"),
    RepoDetails("fhdds-frontend", "FHDDS", "0.5.0", new DateTime("2017-07-09"), "https://raw.githubusercontent.com/hmrc/fhdds-frontend/master/README.md"),
    RepoDetails("fhdds-frontend", "FHDDS", "0.4.0", new DateTime("2017-06-29"), "https://raw.githubusercontent.com/hmrc/fhdds-frontend/master/README.md"),
    RepoDetails("soft-drinks-industry-levy", "Soft Drinks Industry Levy", "3.2.0", new DateTime("2017-07-19"), "https://raw.githubusercontent.com/hmrc/soft-drinks-industry-levy/master/README.md"),
    RepoDetails("soft-drinks-industry-levy-frontend", "Soft Drinks Industry Levy", "2.7.0", new DateTime("2017-07-07"), "https://raw.githubusercontent.com/hmrc/soft-drinks-industry-levy-frontend/master/README.md"),
    RepoDetails("soft-drinks-industry-levy-frontend", "Soft Drinks Industry Levy", "2.6.0", new DateTime("2017-07-05"), "https://raw.githubusercontent.com/hmrc/soft-drinks-industry-levy-frontend/master/README.md"),
    RepoDetails("soft-drinks-industry-levy-frontend", "Soft Drinks Industry Levy", "2.5.0", new DateTime("2017-07-03"), "https://raw.githubusercontent.com/hmrc/soft-drinks-industry-levy-frontend/master/README.md"),
    RepoDetails("soft-drinks-industry-levy-frontend", "Soft Drinks Industry Levy", "2.4.0", new DateTime("2017-06-29"), "https://raw.githubusercontent.com/hmrc/soft-drinks-industry-levy-frontend/master/README.md"),
    RepoDetails("soft-drinks-industry-levy-frontend", "Soft Drinks Industry Levy", "2.3.0", new DateTime("2017-06-19"), "https://raw.githubusercontent.com/hmrc/soft-drinks-industry-levy-frontend/master/README.md"),
    RepoDetails("soft-drinks-industry-levy-frontend", "Soft Drinks Industry Levy", "2.2.0", new DateTime("2017-06-09"), "https://raw.githubusercontent.com/hmrc/soft-drinks-industry-levy-frontend/master/README.md"),
    RepoDetails("soft-drinks-industry-levy-frontend", "Soft Drinks Industry Levy", "2.1.0", new DateTime("2017-06-05"), "https://raw.githubusercontent.com/hmrc/soft-drinks-industry-levy-frontend/master/README.md")
  )

  def getAddAService: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(add_a_service(releaseForm)))
  }

  def submitAddAService: Action[AnyContent] = Action.async { implicit request =>
    Future successful Redirect(routes.LoginController.getServiceList())
  }

  def getServiceList: Action[AnyContent] = Action.async { implicit request =>
    val serviceNameList = serviceDetailsList.map(_.serviceName).distinct
    Future.successful(Ok(service_list(serviceNameList)))
  }

  def getServiceReleaseNotes(serviceName: String): Action[AnyContent] = Action.async { implicit request =>
    val ser = serviceDetailsList.filter(_.serviceName == serviceName)
    Future.successful(Ok(service_release_note(ser)))
  }

  def getRepoReleaseNotes(repoName: String): Action[AnyContent] = Action.async { implicit request ⇒
    val filteredList = serviceDetailsList.filter(_.repoName == repoName).head
    val serviceLink = filteredList.link
    val serviceName = filteredList.serviceName

    val releaseInfo = Source.fromURL(serviceLink).getLines()

    Future.successful(Ok(get_release_note(releaseInfo, serviceName)))
  }

  def getRepoList: Action[AnyContent] = Action.async { implicit request ⇒
    val repoNameList = serviceDetailsList.map(_.repoName).distinct
    val filteredRepos = serviceDetailsList.groupBy(_.repoName)
    val latestReleases = repoNameList.map { name ⇒
      filteredRepos(name).maxBy(_.deployDate)
    }
    Future.successful(Ok(repo_list(latestReleases)))
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
      hasValidToken {
        furtherAction(request)
      } {
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
