package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, Controller}
import uk.gov.hmrc.SimulatingFUAS.{BackConnector, FrontConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global

object FilesController extends FilesController

trait FilesController extends Controller with FrontendController  {
  val frontConnector = FrontConnector
  val backConnector = BackConnector

  val main = Action.async {
    implicit request =>
      var files:List[File] = List.empty

      backConnector.loadFiles.map {
        resultFromBackEnd =>

          val envelopeId = resultFromBackEnd.\\("envelopeId").map(_.as[String]).toList
          val fileId = resultFromBackEnd.\\("fileId").map(_.as[String]).toList
          val fileRef = resultFromBackEnd.\\("_id").map(_.as[String]).toList
          val startedAt = resultFromBackEnd.\\("startedAt").map(_.as[Long]).toList

          for (i <- 0 to envelopeId.length-1) {
            files = files :+ File(envelopeId(i), fileId(i), fileRef(i), startedAt(i).toString)
          }

          Ok(uk.gov.hmrc.SimulatingFUAS.views.html.file_main(files)).withHeaders()
      }
  }
}

case class File(EnvelopeID: String, FileID: String, FileRef: String, startedAt: String)