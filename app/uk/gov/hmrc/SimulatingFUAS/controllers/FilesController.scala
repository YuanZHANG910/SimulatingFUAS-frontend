package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.mvc.{Action, Controller}
import uk.gov.hmrc.SimulatingFUAS.{BackConnector, FrontConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object FilesController extends FilesController

trait FilesController extends Controller with FrontendController  {
  val frontConnector = FrontConnector
  val backConnector = BackConnector

  val main = Action.async {
    implicit request =>
      Future.successful(Ok(uk.gov.hmrc.SimulatingFUAS.views.html.file_main()).withHeaders())
  }
}
