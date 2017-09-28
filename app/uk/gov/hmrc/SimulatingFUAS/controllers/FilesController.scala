package uk.gov.hmrc.SimulatingFUAS.controllers

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import uk.gov.hmrc.SimulatingFUAS.models.FileInProgress
import uk.gov.hmrc.SimulatingFUAS.supports.{BackConnector, FrontConnector}
import uk.gov.hmrc.SimulatingFUAS.views.html.file_views._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future


object FilesController extends Controller with FrontendController  {

  val frontConnector: FrontConnector.type = FrontConnector
  val backConnector: BackConnector.type = BackConnector

  def getFilesInProgress(implicit request: Request[AnyContent]): Future[Result] = {
    backConnector.loadInProgressFiles.map {
      resultFromBackEnd =>
        val filesInProgress = FileInProgress.getFilesInprogress(resultFromBackEnd)
        Ok(file_main(filesInProgress))
    }
  }

  def main(): Action[AnyContent] = Action.async {
    implicit request =>
      getFilesInProgress
  }

  def deleteInProgressFile(fileRef: String): Action[AnyContent] = Action.async {
    implicit request =>
      backConnector.deleteInProgressFile(fileRef)
      getFilesInProgress
  }

  def scan(envelopeId: String, fileId: String, fileRef: String): Action[AnyContent] = Action.async {
    implicit request =>
      frontConnector.scan(envelopeId, fileId, fileRef)
      getFilesInProgress
  }

  def moveToTransientStore(envelopeId: String, fileId: String, fileRef: String): Action[AnyContent] = Action.async {
    implicit request =>
      frontConnector.moveToTransientStore(envelopeId, fileId, fileRef)
      getFilesInProgress
  }

}
