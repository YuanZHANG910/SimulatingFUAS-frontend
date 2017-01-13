package uk.gov.hmrc.SimulatingFUAS.supports

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.SimulatingFUAS.config.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost}

import scala.concurrent.Future

object BackConnector extends BackConnector with ServicesConfig with ActionsSupport {

  lazy val Url: String = baseUrl("file-back")
  val http = WSHttp

  def createAnEmptyEnvelope(implicit hc: HeaderCarrier): Future[String] = {
    val emptyJson = Json.obj()
    val res = http.POST(s"$Url/file-upload/envelopes", emptyJson).map(res => res.header("Location").last)
    res
  }

  def loadEnvelopeInf(eid: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
    val res = http.GET(s"$Url/file-upload/envelopes/$eid").map(response => response.json)
    res
  }

  def loadEnvelopeEve(eid: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
    val res = http.GET(s"$Url/file-upload/events/$eid").map(response => response.json)
    res
  }

  def loadFiles(implicit hc: HeaderCarrier): Future[JsValue] = {
    val res = http.GET(s"$Url/file-upload/files/inprogress").map(response => response.json)
    res
  }
}

trait BackConnector {
  val Url: String
  val http: HttpGet with HttpPost
}
