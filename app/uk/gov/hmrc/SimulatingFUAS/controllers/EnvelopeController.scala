package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, Controller}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.libs.json.Json
import play.api.Play.current
import uk.gov.hmrc.SimulatingFUAS.supports.{BackConnector, FrontConnector}

import scala.concurrent.Future


case class UserInput(input: String)

object EnvelopeController extends EnvelopeController

trait EnvelopeController extends Controller with FrontendController {
  val frontConnector = FrontConnector
  val backConnector = BackConnector

  val inputEnvelopesId : Form[UserInput] =
    Form(mapping(
      "input" -> nonEmptyText
    )(UserInput.apply)(UserInput.unapply))

  val main = Action.async {
    implicit request =>
      Future.successful(Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelope_main("")(request, applicationMessages)).withHeaders())
  }

  def loadEnvelopeInf = Action.async {
    implicit request =>
      val submitInput = inputEnvelopesId.bindFromRequest()
      val result = backConnector.loadEnvelopeInf(submitInput.data("envelopes'ID")).map {
        resultFromBackEnd =>
          Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelopes_inf(submitInput.data("envelopes'ID"))(Json.prettyPrint(resultFromBackEnd))(request, applicationMessages))
      }
      result.recover{
        case _ => Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelope_main(Json.prettyPrint(Json.toJson("For envelopes' id:"+submitInput.data("envelopes'ID")+" is not-found")))(request, applicationMessages))
      }
  }

  def loadEnvelopeInfR(id: String, inf: String) = Action {
    implicit request =>
      Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelopes_inf(id)(inf)(request, applicationMessages))
  }

  def loadEnvelopeInfRE(id: String) = Action.async {
    implicit request =>
    backConnector.loadEnvelopeInf(id).map {
      resultFromBackEnd =>
        Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelopes_inf(id)(Json.prettyPrint(resultFromBackEnd))(request, applicationMessages))
    }
  }

  def loadEnvelopeEve(id: String) = Action.async {
    implicit request =>
      backConnector.loadEnvelopeEve(id).map {
        resultFromBackEnd =>
          Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelopes_eve(id)(Json.prettyPrint(resultFromBackEnd))(request, applicationMessages))
      }
  }

  def replay(id: String, inf: String) = Action.async {
    implicit request =>
      backConnector.loadEnvelopeEve(id).map {
        resultFromBackEnd =>
          Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelopes_eve(id)(Json.prettyPrint(resultFromBackEnd))(request, applicationMessages))
      }
  }


}
