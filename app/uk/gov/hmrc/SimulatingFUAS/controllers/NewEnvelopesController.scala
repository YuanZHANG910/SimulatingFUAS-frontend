package uk.gov.hmrc.SimulatingFUAS.controllers

import java.net.URLEncoder

import play.api.Logger
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.libs.ws.WSResponse
import play.api.mvc._
import uk.gov.hmrc.SimulatingFUAS.models.{Forms, SaveFile, UserFileActionInput, UserInput}
import uk.gov.hmrc.SimulatingFUAS.supports.{BackConnector, FrontConnector}
import uk.gov.hmrc.SimulatingFUAS.views.html.envelope_journey_views._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

object NewEnvelopesController extends Controller with FrontendController {

  val frontConnector: FrontConnector.type = FrontConnector
  val backConnector: BackConnector.type = BackConnector

  val inputEnvelopesBody: Form[UserInput] = Forms.userInputForm
  val userFileActionInputForm: Form[UserFileActionInput] = Forms.userFileActionInputForm

  def startAnJourney: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(an_envelope_journey("")))
  }

  def callCreateAnEnvelope: Action[AnyContent] = Action.async {
    implicit request =>
      backConnector.createAnEmptyEnvelope match {
        case error: Throwable ⇒ Future.successful(Ok(an_envelope_journey(s"invalid Json $error")))
        case resultFromBackEnd: Future[WSResponse] ⇒
          resultFromBackEnd.flatMap {
            resultFromBackEnd ⇒
              if (resultFromBackEnd.status == 201) {
                val result = resultFromBackEnd.header("Location").last.split("/").toList
                val envelopeId = result.last
                Future.successful(Ok(create_an_envelope_and_upload(envelopeId, "")))
              } else {
                Future.successful(
                  Ok(an_envelope_journey(s"${resultFromBackEnd.json}"))
                )
              }
          }
        case _ ⇒ Future.successful(Ok(an_envelope_journey(s"Unknown Error")))
      }
  }

  def deleteAnEnvelope(id: String): Action[AnyContent] = Action.async {
    implicit request =>
      backConnector.deleteAnEnvelope(id).flatMap {
        _ ⇒ {
          val message = s"Envelope: $id has been deleted"
          Future.successful(Ok(an_envelope_journey(message)))
        }
      }
  }

  def upLoadingFiles(eid: String): Action[AnyContent] = Action.async {
    implicit request =>
      frontConnector.upLoadFiles(eid)(request.body.asMultipartFormData) match {
        case Left(error) ⇒ Future.successful(Ok(create_an_envelope_and_upload(eid, s"File failed to uploaded: $error")))
        case Right(response) ⇒
          response.map{
            case response: WSResponse =>
              if(response.status == 200) Ok(upload_download_file(eid,
                                            "File has been uploaded, please check your callback url or view the Envelope, " +
                                              "or upload more files",
                                            "", ""))
              else Ok(create_an_envelope_and_upload(eid, s"File failed to uploaded: ${response.json.toString()}"))
            case _ ⇒ Ok(create_an_envelope_and_upload(eid, "File failed to uploaded: Unknown Error"))
        }
      }
  }

  def downloadOrDeleteFiles(eid: String): Action[AnyContent] = Action.async {
    implicit request =>
      val fileId = userFileActionInputForm.bindFromRequest().data("fileId")
      val actionType = userFileActionInputForm.bindFromRequest().data("action")

      if (fileId.isEmpty) {
        Future.successful(
          Ok(upload_download_file(eid,
            "Upload more files, if you want to",
            "Please enter a fileId", ""))
        )
      } else {
        actionType match {
          case "download" ⇒ downloadFile(eid, fileId)
          case "delete" ⇒ deleteFile(eid, fileId)
        }
      }
  }

  def downloadFile(eid: String, fileId: String)(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Result] = {
    val encodedFileId = URLEncoder.encode(fileId, "UTF-8")
    backConnector.downloadFile(eid, encodedFileId).flatMap {
      resultFromBackEnd ⇒
        if (resultFromBackEnd.status == 200) {
          SaveFile.saveFileToLocal(resultFromBackEnd, fileId)
          Future.successful(
            Ok(upload_download_file(eid,
              "Upload more files, if you want to",
              s"$fileId has saved at ./tmp/$fileId, or download it use your browser " +
                s"${backConnector.Url}/file-upload/envelopes/$eid/files/$encodedFileId/content", "")
            )
          )
        } else {
          Future.successful(
            Ok(upload_download_file(eid,
              "Upload more files, if you want to",
              s"${resultFromBackEnd.json}",
              ""))
          )
        }
    }
  }

  def deleteFile(eid: String, fileId: String)(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Result] = {
    val encodedFileId = URLEncoder.encode(fileId, "UTF-8")
    backConnector.deleteFile(eid, encodedFileId).flatMap {
      resultFromBackEnd ⇒
        if (resultFromBackEnd.status == 200) {
          Future.successful(
            Ok(upload_download_file(eid,
              "Upload more files, if you want to",
              s"File:$fileId has been deleted",
              ""))
          )
        } else {
          Future.successful(
            Ok(upload_download_file(eid,
              "Upload more files, if you want to",
              s"${resultFromBackEnd.json}",
              ""))
          )
        }

    }
  }

  def routeAnEnvelope(eid: String): Action[AnyContent] = Action.async {
    implicit request =>
      backConnector.routeAnEnvelope(eid) match {
        case error: Throwable ⇒ Future.successful(
                                  Ok(upload_download_file(eid,
                                                          "Upload more files, if you want to",
                                                          "",
                                                          s"invalid Json $error"))
                                )
        case resultFromBackEnd: Future[HttpResponse] ⇒
          resultFromBackEnd.flatMap {
            _ ⇒
              Future.successful(
                Ok(Envelope_routed(eid,
                                   "The envelope has been routed, can not upload more files to the envelope," +
                                     " but you still can download a file with a fileID, or download the envelope as a zip file",
                                   s""))
              )
          }
      }
  }

  def downloadEnvelope(eid: String): Action[AnyContent] = Action.async {
    implicit request =>
      val zipName = s"$eid.zip"
      backConnector.downloadEnvelope(eid).flatMap {
        resultFromBackEnd ⇒
          SaveFile.saveFileToLocal(resultFromBackEnd, zipName)
          Future.successful(
            Ok(Envelope_routed(eid,
                               "The envelope has been routed, can not upload more files to the envelope," +
                                 " but you still can download a file with a fileID, or download the envelope as a zip file",
                               s"$eid has saved at ./tmp/$zipName, or download it use your browser " +
                                 s"${backConnector.Url}/file-transfer/envelopes/$eid")
            )
          )
      }
  }

  def downloadFileAfterRouted(eid: String): Action[AnyContent] = Action.async {
    implicit request =>
    val fileId = userFileActionInputForm.bindFromRequest().data("fileId")
    val encodedFileId = URLEncoder.encode(fileId, "UTF-8")
    backConnector.downloadFile(eid, encodedFileId).flatMap {
      resultFromBackEnd ⇒
        SaveFile.saveFileToLocal(resultFromBackEnd, fileId)
        Future.successful(
          Ok(Envelope_routed(eid,
            "The envelope has been routed, can not upload more files, but you still can download a file with a fileID",
            s"$fileId has saved at ./tmp/$fileId, or download it use your browser " +
              s"${backConnector.Url}/file-upload/envelopes/$eid/files/$encodedFileId/content")
          )
        )
    }
  }

  def deleteRoutedEnvelope(eid: String): Action[AnyContent] = Action.async {
    implicit request =>
      backConnector.deleteRoutedEnvelope(eid).flatMap {
        _ ⇒
          val message = s"Envelope: $eid has been deleted, a full life cycle of an envelope has fulfilled"
          Future.successful(Ok(an_envelope_journey(message)))
      }
  }

  def callBack: Action[AnyContent] = Action {
    implicit request =>
      Logger.info(s"${request.body}")
      Ok("request")
  }
}