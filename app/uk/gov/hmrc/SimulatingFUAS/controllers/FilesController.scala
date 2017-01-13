package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.hmrc.SimulatingFUAS.controllers.LoginController.securedAction
import uk.gov.hmrc.SimulatingFUAS.models.FileInProgress
import uk.gov.hmrc.SimulatingFUAS.supports.{BackConnector, FrontConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController


object FilesController extends Controller with FrontendController  {
  val frontConnector = FrontConnector
  val backConnector = BackConnector

  val main: Action[AnyContent] = securedAction[AnyContent] {
    implicit request =>

      backConnector.loadFiles.map {
        resultFromBackEnd =>
          var files:List[FileInProgress] = List.empty
          val envelopeId = resultFromBackEnd.\\("envelopeId").map(_.as[String]).toList
          val fileId = resultFromBackEnd.\\("fileId").map(_.as[String]).toList
          val fileRef = resultFromBackEnd.\\("_id").map(_.as[String]).toList
          val startedAt = resultFromBackEnd.\\("startedAt").map(_.as[Long]).toList

          for (i <- envelopeId.indices) {
            files = files :+ FileInProgress(envelopeId(i), fileId(i), fileRef(i), startedAt(i).toString)
          }

          Ok(uk.gov.hmrc.SimulatingFUAS.views.html.file_main(files)(request, applicationMessages)).withHeaders()
      }

  }
}
