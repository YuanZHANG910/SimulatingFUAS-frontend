package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.hmrc.SimulatingFUAS.models.{Forms, UserInput}
import uk.gov.hmrc.SimulatingFUAS.supports.BackConnector
import uk.gov.hmrc.SimulatingFUAS.views.html.envelope_views._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object EnvelopeController extends Controller with FrontendController {

  val backConnector: BackConnector.type = BackConnector
  val inputEnvelopesId : Form[UserInput] = Forms.userInputForm

  val main: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(envelope_main("")))
  }

  def loadEnvelopeInf: Action[AnyContent] = Action.async {
    implicit request =>
      val submitInput = inputEnvelopesId.bindFromRequest()
      val result = backConnector.loadEnvelopeInf(submitInput.data("envelopes'ID")).map {
        resultFromBackEnd =>
          Ok(envelopes_inf(submitInput.data("envelopes'ID"), Json.prettyPrint(resultFromBackEnd)))
      }
      result.recover{
        case _ => Ok(envelope_main("For envelope: "+submitInput.data("envelopes'ID")+" is not-found"))
      }
  }

  def loadEnvelopeInfR(id: String, inf: String): Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(envelopes_inf(id, inf)))
  }

  def loadEnvelopeInfRE(id: String): Action[AnyContent] = Action.async {
    implicit request =>
    backConnector.loadEnvelopeInf(id).map {
      resultFromBackEnd =>
        Ok(envelopes_inf(id, Json.prettyPrint(resultFromBackEnd)))
    }
  }

  def loadEnvelopeEve(id: String): Action[AnyContent] = Action.async {
    implicit request =>
      backConnector.loadEnvelopeEve(id).map {
        resultFromBackEnd =>
          Ok(envelopes_inf(id, Json.prettyPrint(resultFromBackEnd)))
      }
  }

  def replay(id: String): Action[AnyContent] = Action.async {
    implicit request =>
      backConnector.loadEnvelopeEve(id).map {
        resultFromBackEnd =>
          Ok(envelopes_inf(id, Json.prettyPrint(resultFromBackEnd)))
      }
  }

}
