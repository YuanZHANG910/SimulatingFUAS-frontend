package uk.gov.hmrc.SimulatingFUAS

import play.api.libs.json.Json
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by yuan on 05/10/16.
  */
object FrontConnector extends FrontConnector with ServicesConfig {
  lazy val Url = baseUrl("file-front")
  val http = WSHttp
}

trait FrontConnector{
  val Url: String
  val http: HttpGet with HttpPost

  def createAnEmptyEnvelope(implicit hc: HeaderCarrier): Future[String] = {
    val emptyJson = Json.obj()
    val res = http.POST(s"$Url/file-upload/envelopes", emptyJson).map( res => res.header("Location").last)
    res
  }
}




