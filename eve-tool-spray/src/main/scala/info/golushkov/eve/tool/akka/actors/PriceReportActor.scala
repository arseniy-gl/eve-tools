package info.golushkov.eve.tool.akka.actors

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import info.golushkov.eve.tool.akka.actors.mongo.{ItemActor, OrdersActor, PriceActor}
import info.golushkov.eve.tool.akka.models.{Item, Order, Price, PriceReportRow}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class PriceReportActor(itemActor: ActorRef, priceActor: ActorRef, ordersActor: ActorRef) extends Actor {
  import PriceReportActor._
  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val to: Timeout = Timeout(5 seconds)

  private def withContext(
                           _queue: List[(ActorRef, MakeReport)] = Nil,
                           _working: Boolean = false,
                           _prices: List[Price] = Nil,
                           _items: List[Item] = Nil,
                           _orders: List[Order] = Nil): Actor.Receive = {
    case MakeReport(id) =>
      if(_working) {
        context.become(withContext(_queue :+ (sender() -> MakeReport(id)), _working, _prices, _items, _orders))
      } else {
        context.become(withContext(_working = true))
        itemActor ! ItemActor.GetAllOnMarketGroup(id)
        priceActor ! PriceActor.GetAll
      }

    case items:List[Item] =>
      context.become(withContext(_queue, _working = true, _prices, items, _orders))
      items.foreach { i =>
        ordersActor ! OrdersActor.GetOnItemId(i.id)
      }
  }

  override def receive = {
    case MakeReport(id) =>
      (for {
        items <- (itemActor ? ItemActor.GetAllOnMarketGroup(id)).map(_.asInstanceOf[List[Item]])
        prices <- (priceActor ? PriceActor.GetAll).map(_.asInstanceOf[List[Price]])
        ordersWithItem <- Future.sequence {
          items.map { i =>
            (ordersActor ? OrdersActor.GetOnItemId(i.id))
              .map { res =>
                i -> res.asInstanceOf[List[Order]]
              }
          }
        }
      } yield {
        ordersWithItem.map {
          case (item, orders) =>
            PriceReportRow(
              name = item.name,
              price = prices.find(p => p.typeId == item.id).map(_.adjustedPrice).getOrElse(0),
              lowBuy = orders.filter(_.isBuy).map(_.price).min,
              highBuy = orders.filter(_.isBuy).map(_.price).max,
              lowSell = orders.filterNot(_.isBuy).map(_.price).min,
              highSell = orders.filterNot(_.isBuy).map(_.price).max)
        }
      }) pipeTo sender()

  }
}

object PriceReportActor {
  case class MakeReport(groupId: Int)
}
