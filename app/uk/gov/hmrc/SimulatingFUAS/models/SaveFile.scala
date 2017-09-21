package uk.gov.hmrc.SimulatingFUAS.models

import java.io.{BufferedOutputStream, File, FileOutputStream}

import uk.gov.hmrc.play.http.HttpResponse

object SaveFile {
  def saveFileToLocal(resultFromBackEnd: HttpResponse, fileId:String): Unit = {
    val fileBody = resultFromBackEnd.body
    val outputFolder = s"./tmp"
    val pathFile: File = new File(outputFolder)
    if (!pathFile.exists) pathFile.mkdirs

    val bos = new BufferedOutputStream(new FileOutputStream(s"$outputFolder/$fileId"))
    Stream.continually(bos.write(fileBody.getBytes))
    bos.close()
  }
}
