package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.mvc._
import uk.gov.hmrc.SimulatingFUAS.{BackConnector, FrontConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object NewEnvelopesController extends NewEnvelopesController

trait NewEnvelopesController extends Controller with FrontendController {
  val frontConnector = FrontConnector
  val backConnector = BackConnector

  val callCreateAnEnvelope = Action.async {
    implicit request =>
      backConnector.createAnEmptyEnvelope.map {
        resultFromBackEnd =>
          val t = resultFromBackEnd.split("/").toList
          val envelopeId = t.last
          Ok(uk.gov.hmrc.SimulatingFUAS.views.html.got_envelopes_id(envelopeId)(request, applicationMessages))
      }
  }

  def upLoadingFiles(eid:String) = Action.async {
    implicit request =>
      frontConnector.upLoadFiles(eid, request.headers)(request.body.asMultipartFormData)
      Future.successful(Ok(uk.gov.hmrc.SimulatingFUAS.views.html.got_envelopes_id(eid)(request, applicationMessages)))
  }
}
