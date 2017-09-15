package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.hmrc.SimulatingFUAS.controllers.LoginController._
import uk.gov.hmrc.SimulatingFUAS.supports.{BackConnector, FrontConnector}
import uk.gov.hmrc.SimulatingFUAS.views.html.envelope_views._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object NewEnvelopesController extends Controller with FrontendController {
  val frontConnector = FrontConnector
  val backConnector = BackConnector

  val callCreateAnEnvelope: Action[AnyContent] = securedAction[AnyContent] {
    implicit request =>
      backConnector.createAnEmptyEnvelope.map {
        resultFromBackEnd =>
          val t = resultFromBackEnd.split("/").toList
          val envelopeId = t.last
          Ok(got_envelopes_id(envelopeId))
      }
  }

  def upLoadingFiles(eid:String): Action[AnyContent] = securedAction[AnyContent] {
    implicit request =>
      frontConnector.upLoadFiles(eid, request.headers)(request.body.asMultipartFormData)
      Future.successful(Ok(got_envelopes_id(eid)))
  }
}
