package uk.gov.hmrc.SimulatingFUAS.controllers

import com.github.nscala_time.time.Imports.DateTime
import com.github.nscala_time.time.OrderingImplicits._
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.SimulatingFUAS.controllers.LoginController.{Ok, Redirect}
import uk.gov.hmrc.SimulatingFUAS.models.{ReleaseNote, RepoDetails}
import uk.gov.hmrc.SimulatingFUAS.views.html.release_views._

import scala.concurrent.Future
import scala.io.Source

object ReleaseRepoController {
  val releaseForm: Form[ReleaseNote] = ReleaseNote.releaseForm

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
    Future successful Redirect(routes.ReleaseRepoController.getServiceList())
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
      filteredRepos(name).maxBy(date ⇒ date.deployDate)
    }
    Future.successful(Ok(repo_list(latestReleases)))
  }
}