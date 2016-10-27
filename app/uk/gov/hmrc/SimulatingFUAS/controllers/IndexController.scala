package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.Play.current

import scala.concurrent.Future

object IndexController extends IndexController

trait IndexController extends Controller with FrontendController {

  val index = Action.async {
    implicit request =>
		Future.successful(Ok(uk.gov.hmrc.SimulatingFUAS.views.html.index()(request, applicationMessages)).withHeaders())
  }

  val about = Action.async {
    implicit request =>
      Future.successful(Ok(uk.gov.hmrc.SimulatingFUAS.views.html.about()(request, applicationMessages)).withHeaders())
  }

}
