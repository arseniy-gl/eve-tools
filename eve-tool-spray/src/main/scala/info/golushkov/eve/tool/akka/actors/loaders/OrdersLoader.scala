package info.golushkov.eve.tool.akka.actors.loaders

import akka.actor.{Actor, ActorLogging, ActorRef}
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.ApiActor.ResultGetMarketsRegionIdOrders
import info.golushkov.eve.tool.akka.actors.mongo.RegionActor.GetAllResult
import info.golushkov.eve.tool.akka.actors.mongo.{OrdersActor, RegionActor}
import info.golushkov.eve.tool.akka.models.{Order, Region}

class OrdersLoader(ordersActor: ActorRef, regionActor: ActorRef, api: ActorRef) extends Actor with ActorLogging {
  import OrdersLoader._
  private var regions: List[Region] = Nil

  override def receive = {
    case Update =>
      log.info(s"Update - start!")
      ordersActor ! OrdersActor.ClearAll
      regionActor ! RegionActor.GetAll

    case GetAllResult(_regions) =>
      log.info(s"regions - ${_regions.size}!")
      this.regions = _regions
      self ! Next

    case ResultGetMarketsRegionIdOrders(orders) =>
      log.info(s"loading orders - ${orders.size}!")
      orders.foreach { o =>
        ordersActor ! OrdersActor.WriteOrUpdate(o)
      }
      self ! Next

    case Next =>
      regions match {
        case region :: tail =>
          regions = tail
          api ! ApiActor.GetMarketsRegionIdOrders(regionId = region.id)

        case Nil => ()
      }

  }

  private case object Next
}

object OrdersLoader {
  case object Update
}
