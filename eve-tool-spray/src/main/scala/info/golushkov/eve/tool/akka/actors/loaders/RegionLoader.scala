package info.golushkov.eve.tool.akka.actors.loaders

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.util.Timeout
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.ApiActor.ResultAsIdsList
import info.golushkov.eve.tool.akka.actors.mongo.RegionActor
import info.golushkov.eve.tool.akka.models._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class RegionLoader(regionActor: ActorRef, api: ActorRef) extends Actor with ActorLogging {
  import RegionLoader._
  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val to: Timeout = Timeout(5 seconds)
  private var regionIds: List[Int] = Nil //TODO переделать на контекст

  override def receive: Receive = {
    case Update =>
      log.info(s"Update - start!")
      api ! ApiActor.GetUniverseRegions

    case ResultAsIdsList(ids) =>
      log.info(s"Load regionIds [${ids.size}]")
      regionIds = ids
      self ! Next

    case Next =>
      regionIds match {
        case id :: tail =>
          regionIds = tail
          api ! ApiActor.GetUniverseRegionsRegionId(id)

        case Nil => ()
      }

    case r: Region =>
      log.info(s"Load region")
      regionActor ! RegionActor.WriteOrUpdate(r)
      self ! Next
  }

  private case object Next
}

object RegionLoader {

  case object Update

}
