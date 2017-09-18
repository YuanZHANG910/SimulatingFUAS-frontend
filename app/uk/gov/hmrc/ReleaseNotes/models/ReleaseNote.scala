package uk.gov.hmrc.ReleaseNotes.models

import com.github.nscala_time.time.Imports.DateTime
import play.api.data.Form
import play.api.data.Forms._

object ReleaseNote {
  val releaseForm = Form (
    mapping (
      "name" -> nonEmptyText,
      "url" -> nonEmptyText,
      "serversName" -> nonEmptyText,
      "version" -> nonEmptyText
    )
    (ReleaseNote.apply)(ReleaseNote.unapply)
  )
}
case class ReleaseNote(name: String, url: String, serversName: String, version: String)
case class RepoDetails(repoName: String, serviceName: String, version: String, deployDate: DateTime, link: String)