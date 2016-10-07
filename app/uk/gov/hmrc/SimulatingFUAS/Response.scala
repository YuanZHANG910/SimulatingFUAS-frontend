package uk.gov.hmrc.SimulatingFUAS

import play.api.libs.json.Json

/**
  * Created by yuan on 06/10/16.
  */
case class Response(resp : String)

object Response {
  implicit val format = Json.format[Response]
}