package uk.gov.hmrc.SimulatingFUAS.supports

import java.net.URLEncoder
import java.nio.file.Paths

import play.api.mvc.{Headers, MultipartFormData}
import uk.gov.hmrc.SimulatingFUAS.config.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost}

object FrontConnector extends FrontConnector with ServicesConfig with ActionsSupport {

  lazy val Url = baseUrl("file-front")
  val http = WSHttp

  def upLoadFiles(eId:String, requestHeader: Headers)(files: Option[MultipartFormData[play.api.libs.Files.TemporaryFile]])(implicit headerCarrier: HeaderCarrier): Unit = {

    for (file <- files.map(_.files).toList(0)) {
      val path = Paths.get(file.ref.file.getAbsolutePath)
      val data = java.nio.file.Files.readAllBytes(path)
      val encodedFileName = URLEncoder.encode(file.filename)

      client
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

trait FrontConnector{
  val Url: String
  val http: HttpGet with HttpPost
}




