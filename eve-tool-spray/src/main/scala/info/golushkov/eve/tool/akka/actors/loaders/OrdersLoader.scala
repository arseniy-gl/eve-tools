package info.golushkov.eve.tool.akka.actors.loaders

import akka.actor.{Actor, ActorLogging, ActorRef}
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.mongo.{OrdersActor, RegionActor}
import info.golushkov.eve.tool.akka.models.{Order, Region}

class OrdersLoader(ordersActor: ActorRef, regionActor: ActorRef, api: ActorRef) extends Actor with ActorLogging {
  import OrdersLoader._

  override def receive = {
    case Update =>
      log.info(s"Update - start!")
      regionActor ! RegionActor.GetAll

    case regions: List[Region] =>
      log.info(s"regions - ${regions.size}!")
      regions match {
        case region :: tail =>
          api ! ApiActor.GetMarketsRegionIdOrders(regionId = region.id)
          self ! tail

        case Nil => ()
      }

    case orders: List[Order] =>
      log.info(s"loading orders - ${orders.size}!")
      orders.foreach { o =>
        ordersActor ! OrdersActor.WriteOrUpdate(o)
      }
  }
}

object OrdersLoader {
  case object Update
}
