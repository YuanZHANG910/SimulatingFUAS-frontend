package uk.gov.hmrc.SimulatingFUAS.controllers

import java.net.URLEncoder

import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.libs.ws.WSResponse
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.hmrc.SimulatingFUAS.controllers.LoginController._
import uk.gov.hmrc.SimulatingFUAS.models.{Forms, SaveFile, UserInput}
import uk.gov.hmrc.SimulatingFUAS.supports.{BackConnector, FrontConnector}
import uk.gov.hmrc.SimulatingFUAS.views.html.envelope_journey_views._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object NewEnvelopesController extends Controller with FrontendController {

  val frontConnector: FrontConnector.type = FrontConnector
  val backConnector: BackConnector.type = BackConnector

  val inputEnvelopesBody: Form[UserInput] = Forms.userInputForm

  def startAnJourney: Action[AnyContent] = securedAction[AnyContent] {
    implicit request =>
      Future.successful(Ok(an_envelope_journey("")))
  }

  def callCreateAnEnvelope: Action[AnyContent] = securedAction[AnyContent] {
    implicit request =>
      backConnector.createAnEmptyEnvelope match {
        case error: Throwable ⇒ Future.successful(Ok(an_envelope_journey(s"invalid Json $error")))
        case resultFromBackEnd: Future[String] ⇒
          resultFromBackEnd.flatMap {
            resultFromBackEnd ⇒
              val result = resultFromBackEnd.split("/").toList
              val envelopeId = result.last
              Future.successful(Ok(create_an_envelope_and_upload(envelopeId, "")))
          }
      }
  }

  def upLoadingFiles(eid: String): Action[AnyContent] = securedAction[AnyContent] {
    implicit request =>
      frontConnector.upLoadFiles(eid)(request.body.asMultipartFormData) match {
        case Left(error) ⇒ Future.successful(Ok(create_an_envelope_and_upload(eid, s"File failed to uploaded: $error")))
        case Right(response) ⇒
          response.map{
            case response: WSResponse =>
              if(response.status == 200) Ok(upload_download_file(eid,
                                            "File has been uploaded, please check your callback url or view the Envelope, " +
                                              "or upload more files",
                                            ""))
              else Ok(create_an_envelope_and_upload(eid, s"File failed to uploaded: ${response.json.toString()}"))
            case _ ⇒ Ok(create_an_envelope_and_upload(eid, "File failed to uploaded: Unknown Error"))
        }
      }
  }

  def downLoadingFiles(eid: String): Action[AnyContent] = securedAction[AnyContent] {
    implicit request =>
      val fileId = inputEnvelopesBody.bindFromRequest().data("fileId")
      val encodedFileId = URLEncoder.encode(fileId, "UTF-8")
      backConnector.downloadFile(eid, encodedFileId) match {
        case resultFromBackEnd ⇒
          resultFromBackEnd.flatMap {
            resultFromBackEnd ⇒
              SaveFile.saveFileToLocal(resultFromBackEnd, fileId)
              Future.successful(
                Ok(upload_download_file(eid,
                  "File has been uploaded, please check your callback url or view the Envelope, or upload more files",
                  s"$fileId has saved at ./tmp/$fileId, or download it use your browser " +
                    s"${backConnector.Url}/file-upload/envelopes/$eid/files/$encodedFileId/content"))
              )
          }
        case error: Throwable ⇒ Future.successful(
                                  Ok(upload_download_file(eid,
                                  "File has been uploaded, please check your callback url or view the Envelope, or upload more files",
                                  s"unable to download the file with error: $error"))
                                )
        case _ ⇒ Future.successful(
                   Ok(upload_download_file(eid,
                     "File has been uploaded, please check your callback url or view the Envelope, or upload more files",
                     s"unable to download the file with unknown error"))
                 )
      }
  }

  def callBack: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok("uploaded"))
  }
}
