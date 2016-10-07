package uk.gov.hmrc.SimulatingFUAS

import play.api.libs.json.Json
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by yuan on 05/10/16.
  */
object BackConnector extends BackConnector with ServicesConfig {
  lazy val Url = baseUrl("file-back")
  val http = WSHttp
}

trait BackConnector {
  val Url: String
  val http: HttpGet with HttpPost

  def createAnEmptyEnvelope(implicit hc: HeaderCarrier): Future[String] = {
    val emptyJson = Json.obj()
    val res = http.POST(s"$Url/file-upload/envelopes", emptyJson).map( res => res.header("Location").last)
    res
  }
}
