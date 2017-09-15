package uk.gov.hmrc.SimulatingFUAS.models

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.QueryStringBindable

object Forms {

  val userLoginForm = Form (
    mapping (
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    )
    (User.apply)(User.unapply)
  )

  val inputEnvelopesId : Form[UserInput] =
    Form(mapping(
      "input" -> nonEmptyText
    )(UserInput.apply)(UserInput.unapply))
}

case class FileInProgress(envelopeID: String, fileID: String, fileRef: String, startedAt: String)

object FileInProgress {

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

case class UserInput(input: String)
case class User(name: String, password: String)