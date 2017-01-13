package uk.gov.hmrc.SimulatingFUAS.supports

import play.api.libs.json.{Json, OFormat}

case class Response(resp : String)

object Response {
  implicit val format: OFormat[Response] = Json.format[Response]
}