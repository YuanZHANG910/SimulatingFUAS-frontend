package uk.gov.hmrc.SimulatingFUAS.supports

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait ActionsSupport {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val akkaSys: ActorSystem = ActorSystem.create()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  val client: AhcWSClient = AhcWSClient()

}
