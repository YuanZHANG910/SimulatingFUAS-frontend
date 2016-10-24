package uk.gov.hmrc.SimulatingFUAS

import java.net.URLEncoder
import java.nio.file.Paths

import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.mvc.{Headers, MultipartFormData}
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

  def upLoadFiles(eId:String, requestHeader: Headers)(files: Option[MultipartFormData[play.api.libs.Files.TemporaryFile]])(implicit headerCarrier: HeaderCarrier): Unit = {

    for (file <- files.map(_.files).toList(0)) {
      val path = Paths.get(file.ref.file.getAbsolutePath)
      val data = java.nio.file.Files.readAllBytes(path)
      val encodedFileName = URLEncoder.encode(file.filename)

      WS
        .url(s"$Url/file-upload/upload/envelopes/$eId/files/$encodedFileName")
        .withHeaders(
          "Content-Type" -> "multipart/form-data; boundary=---011000010111000001101001",
          "X-Request-ID" -> "someId",
          "X-Session-ID" -> "someId",
          "X-Requested-With" -> "someId"
        )
        .post("-----011000010111000001101001\r\nContent-Disposition: form-data; name=\"file1\"; filename=\"" + file.filename + "\"\r\nContent-Type: text/plain\r\n\r\n" + data.mkString + "\r\n-----011000010111000001101001--")
    }
  }
}




