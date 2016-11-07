package uk.gov.hmrc.SimulatingFUAS.supports

import play.api.libs.json.Json

case class Response(resp : String)

object Response {
  implicit val format = Json.format[Response]
}