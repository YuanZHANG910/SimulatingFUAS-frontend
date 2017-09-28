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

  val userInputForm : Form[UserInput] =
    Form(mapping(
      "input" -> nonEmptyText
    )(UserInput.apply)(UserInput.unapply))

  val userFileActionInputForm : Form[UserFileActionInput] =
    Form(mapping(
      "fileId" -> nonEmptyText,
      "action" -> nonEmptyText
    )(UserFileActionInput.apply)(UserFileActionInput.unapply))
}

case class UserInput(input: String)
case class UserFileActionInput(fileId: String, action: String)
case class User(name: String, password: String)