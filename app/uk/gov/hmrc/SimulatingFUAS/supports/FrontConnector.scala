package uk.gov.hmrc.SimulatingFUAS.supports

import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.file.Paths

import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.mvc.{AnyContent, Headers, MultipartFormData, Request}
import uk.gov.hmrc.SimulatingFUAS.config.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

object FrontConnector extends ServicesConfig with ActionsSupport {

  lazy val Url: String = baseUrl("file-front")
  val http = WSHttp
  val emptyJson: JsObject = Json.obj()

  def upLoadFiles(eId:String)(files: Option[MultipartFormData[play.api.libs.Files.TemporaryFile]])
                 (implicit headerCarrier: HeaderCarrier): Either[String,Future[WSResponse]] = {

    val file = files.map(_.files).toList.head.head
    val path = Paths.get(file.ref.file.getAbsolutePath)
    val data = new String(java.nio.file.Files.readAllBytes(path), Charset.forName("UTF-8"))
    val encodedFileName = URLEncoder.encode(file.filename, java.nio.charset.StandardCharsets.UTF_8.toString)
    val contentType = file.contentType.getOrElse("text/plain")

    if (encodedFileName.isEmpty) {Left("Request must have exactly 1 file attached")}
    else {
      Right(
        client
          .url(s"$Url/file-upload/upload/envelopes/$eId/files/$encodedFileName")
          .withHeaders(
            "Content-Type" -> s"$contentType; boundary=---011000010111000001101001"
          )
          .post(s"""-----011000010111000001101001\r\nContent-Disposition:form-data; name="${file.filename}"; filename="${file.filename}"\r\nContent-Type:$contentType\r\n\r\n$data\r\n-----011000010111000001101001--""")
      )
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