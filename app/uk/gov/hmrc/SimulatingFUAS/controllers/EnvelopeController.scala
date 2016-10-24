package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.mvc._
import uk.gov.hmrc.SimulatingFUAS.{BackConnector, FrontConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.libs.json.{Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class UserInput(input: String)

object EnvelopeController extends EnvelopeController

trait EnvelopeController extends Controller with FrontendController {
  val frontConnector = FrontConnector
  val backConnector = BackConnector

  val userInput : Form[UserInput] =
    Form(mapping(
      "input" -> nonEmptyText
    )(UserInput.apply)(UserInput.unapply))

  val main = Action.async {
    implicit request =>
      Future.successful(Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelope_main()).withHeaders())
  }

  def loadEnvelopeInf = Action.async {
    implicit request => val submitInput = userInput.bindFromRequest()
    backConnector.loadEnvelopeInf(submitInput.data("envelopes'ID")).map {
      resultFromBackEnd =>
        Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelopes_inf(submitInput.data("envelopes'ID"))(Json.prettyPrint(resultFromBackEnd)))
    }
  }
  def loadEnvelopeInfR(id: String, inf: String) = Action {
    implicit request =>
      Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelopes_inf(id)(inf))
  }

  def loadEnvelopeInfRE(id: String) = Action.async {
    implicit request =>
    backConnector.loadEnvelopeInf(id).map {
      resultFromBackEnd =>
        Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelopes_inf(id)(Json.prettyPrint(resultFromBackEnd)))
    }
  }

  def loadEnvelopeEve(id: String) = Action.async {
    implicit request =>
      backConnector.loadEnvelopeEve(id).map {
        resultFromBackEnd =>
          Ok(uk.gov.hmrc.SimulatingFUAS.views.html.envelopes_eve(id)(Json.prettyPrint(resultFromBackEnd)))
      }
  }


}
