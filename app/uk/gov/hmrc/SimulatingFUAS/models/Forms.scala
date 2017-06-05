package uk.gov.hmrc.SimulatingFUAS.models

import play.api.data.Form
import play.api.data.Forms._

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

case class FileInProgress(EnvelopeID: String, FileID: String, FileRef: String, startedAt: String)
case class UserInput(input: String)
case class User(name: String, password: String)
