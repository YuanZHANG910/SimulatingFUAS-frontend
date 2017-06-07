package uk.gov.hmrc.SimulatingFUAS.supports

import java.net.URLEncoder
import java.nio.file.Paths

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Headers, MultipartFormData}
import uk.gov.hmrc.SimulatingFUAS.config.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}

import scala.concurrent.Future

object FrontConnector extends FrontConnector with ServicesConfig with ActionsSupport {

  lazy val Url: String = baseUrl("file-front")
  val http = WSHttp
  val emptyJson: JsObject = Json.obj()

  def upLoadFiles(eId:String, requestHeader: Headers)(files: Option[MultipartFormData[play.api.libs.Files.TemporaryFile]])
                 (implicit headerCarrier: HeaderCarrier): Unit = {
    for (file <- files.map(_.files).toList.head) {
      val path = Paths.get(file.ref.file.getAbsolutePath)
      val data = java.nio.file.Files.readAllBytes(path)
      val encodedFileName = URLEncoder.encode(file.filename, java.nio.charset.StandardCharsets.UTF_8.toString)

      client
        .url(s"$Url/file-upload/upload/envelopes/$eId/files/$encodedFileName")
        .withHeaders(
          "Content-Type" -> "multipart/form-data; boundary=---011000010111000001101001",
          "X-Request-ID" -> "someId",
          "X-Session-ID" -> "someId",
          "X-Requested-With" -> "someId"
        )
        .post("-----011000010111000001101001\r\nContent-Disposition: form-data; name=\"file1\"; filename=\"" +
          file.filename + "\"\r\nContent-Type: text/plain\r\n\r\n" + data.mkString + "\r\n-----011000010111000001101001--")
    }
  }

  def scan(envelopeId: String, fileId: String, fileRef: String)
          (implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    http.POST(s"$Url/admin/scan/envelopes/$envelopeId/files/${URLEncoder.encode(fileId, java.nio.charset.StandardCharsets.UTF_8.toString)}/$fileRef", emptyJson)
  }

  def moveToTransientStore(envelopeId: String, fileId: String, fileRef: String)
                          (implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    http.POST(s"$Url/admin/transfer/envelopes/$envelopeId/files/${URLEncoder.encode(fileId, java.nio.charset.StandardCharsets.UTF_8.toString)}/$fileRef", emptyJson)
  }
}

trait FrontConnector{
  val Url: String
  val http: HttpGet with HttpPost
}