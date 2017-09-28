package uk.gov.hmrc.SimulatingFUAS.models

import play.api.libs.json.JsValue
import play.api.mvc.QueryStringBindable

case class FileInProgress(envelopeID: String, fileID: String, fileRef: String, startedAt: String)

object FileInProgress {

  def getFilesInprogress(resultFromBackEnd: JsValue): Seq[FileInProgress] = {
    val envelopeId = resultFromBackEnd.\\("envelopeId").map(_.as[String]).toList
    val fileId = resultFromBackEnd.\\("fileId").map(_.as[String]).toList
    val fileRef = resultFromBackEnd.\\("_id").map(_.as[String]).toList
    val startedAt = resultFromBackEnd.\\("startedAt").map(_.as[Long]).toList

    for (i <- envelopeId.indices) yield {
      FileInProgress(envelopeId(i), fileId(i), fileRef(i), startedAt(i).toString)
    }
  }

  implicit def binderForFileInProgress(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[FileInProgress] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, FileInProgress]] = {
      for {
        envelopeID <- stringBinder.bind("envelopeID", params)
        fileID <- stringBinder.bind("fileID", params)
        fileRef <- stringBinder.bind("fileRef", params)
        startedAt <- stringBinder.bind("startedAt", params)
      } yield {
        (envelopeID, fileID, fileRef, startedAt) match {
          case (Right(envelopeID), Right(fileID), Right(fileRef), Right(startedAt)) => Right(FileInProgress(envelopeID, fileID, fileRef, startedAt))
          case _ => Left("Unable to bind an FileInProgress")
        }
      }
    }
    override def unbind(key: String, fileInProgress: FileInProgress): String = ""
  }

}