package info.golushkov.eve.tool.akka.actors.loaders

import akka.actor.{Actor, ActorLogging, ActorRef}
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.mongo.{OrdersActor, RegionActor}
import info.golushkov.eve.tool.akka.models.{Order, Region}

class OrdersLoader(ordersActor: ActorRef, regionActor: ActorRef, api: ActorRef) extends Actor with ActorLogging {
  import OrdersLoader._
  private var regions: List[Region] = Nil

  override def receive = {
    case Update =>
      log.info(s"Update - start!")
      regionActor ! RegionActor.GetAll

    case any: List[AnyRef] => // TODO костыль (((
      val regions = any.collect { case a: Region => a }
      val orders = any.collect { case a: Order => a }
      if (regions.nonEmpty) {
        log.info(s"regions - ${regions.size}!")
        this.regions = regions
        self ! Next
      } else if (orders.nonEmpty) {
        log.info(s"loading orders - ${orders.size}!")
        orders.foreach { o =>
          ordersActor ! OrdersActor.WriteOrUpdate(o)
        }
        self ! Next
      }

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
