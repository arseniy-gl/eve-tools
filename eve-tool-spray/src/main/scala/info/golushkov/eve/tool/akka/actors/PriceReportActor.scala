package info.golushkov.eve.tool.akka.actors

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import info.golushkov.eve.tool.akka.actors.mongo.RegionActor.GetAllResult
import info.golushkov.eve.tool.akka.actors.mongo.{ItemActor, OrdersActor, PriceActor, RegionActor}
import info.golushkov.eve.tool.akka.models.{Item, Order, Price, PriceReportRow}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class PriceReportActor(
                        itemActor: ActorRef,
                        ordersActor: ActorRef,
                        regionsActor: ActorRef) extends Actor {
  import PriceReportActor._
  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val to: Timeout = Timeout(5 seconds)

  override def receive = {
    case MakeReport(id) =>
      (for {
        items <- (itemActor ? ItemActor.GetOnId(id)).map(_.asInstanceOf[Option[Item]])
        regions <- (regionsActor ? RegionActor.GetAll).map(_.asInstanceOf[GetAllResult].regions)
        ordersWithItem <- Future.sequence {
          items.map { i =>
            (ordersActor ? OrdersActor.GetOnItemId(i.id))
              .map { res =>
                i -> res.asInstanceOf[List[Order]]
              }
          }
        }
      } yield {
        ordersWithItem
          .flatMap {
            case (item, orders) =>
              orders.groupBy(_.regionId).map {
                case (regionId, _orders) =>
                  PriceReportRow(
                    itemName = item.name,
                    regionName = regions.find(_.id == regionId).map(_.name).getOrElse(""),
                    bestBuy = _orders.filter(_.isBuy).map(_.price).max,
                    bestSell = _orders.filterNot(_.isBuy).map(_.price).min)
              }
          }
          .toList
      }) pipeTo sender()

  }
}

object PriceReportActor {
  case class MakeReport(itemId: Int)
}
