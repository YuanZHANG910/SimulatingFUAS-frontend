package uk.gov.hmrc.SimulatingFUAS.supports

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext

trait ActionsSupport {

  implicit val ec = ExecutionContext.global

  implicit val akkaSys = ActorSystem.create()
  implicit val mat = ActorMaterializer()
  val client = AhcWSClient()

}
