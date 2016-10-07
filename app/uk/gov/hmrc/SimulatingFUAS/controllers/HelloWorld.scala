package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.mvc._
import play.mvc.Http.MultipartFormData.FilePart
import uk.gov.hmrc.SimulatingFUAS.{BackConnector, FrontConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.defaultpages.badRequest

import scala.concurrent.Future
import scala.reflect.io.File

object HelloWorld extends HelloWorld

trait HelloWorld extends FrontendController {

  val frontConnector = FrontConnector
  val backConnector = BackConnector

  val helloWorld = Action.async { implicit request =>
		Future.successful(Ok(uk.gov.hmrc.SimulatingFUAS.views.html.hello_world()))
  }

  val callCreateAnEnvelope = Action.async { implicit request =>
    backConnector.createAnEmptyEnvelope.map {
      resultFromBackEnd =>
        val t = resultFromBackEnd.split("/").toList
        val envelopeId = t.last
        Ok(uk.gov.hmrc.SimulatingFUAS.views.html.got_envelopes_id(envelopeId))
    }
  }

  def upLoadingFiles(envelopeId: String) = Action(parse.multipartFormData) {
    implicit request =>
      println("343324324234"+request.body)
      request.body.file("File").map {
        picture =>
          import java.io.File
          val filename = picture.filename
          val contentType = picture.contentType
          picture.ref.moveTo(new File(s"/tmp/picture/$filename"))
          (Ok("File uploaded"))
      }.getOrElse {
        (BadRequest(uk.gov.hmrc.SimulatingFUAS.views.html.got_envelopes_id(envelopeId)).flashing(
          "error" -> "Missing file"))
      }
  }

}
