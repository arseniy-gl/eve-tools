package info.golushkov.eve.tool.akka.actors.loaders

import akka.actor.{Actor, ActorRef}
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.mongo.{ItemActor, RegionActor, TradeHistoryActor}
import info.golushkov.eve.tool.akka.models.{Item, Region, TradeHistory}

class TradeHistoryLoader(
                          tradeHistoryActor: ActorRef,
                          regionActor: ActorRef,
                          itemsActor: ActorRef,
                          api: ActorRef) extends Actor {
  import TradeHistoryLoader._

  def inProcess(items:List[Item] = Nil, regions:List[Region] = Nil): Actor.Receive = {
    case Next =>
      items match {
        case item :: tail =>
          context.become(inProcess(tail, regions))
          regions.foreach { region =>
            api ! ApiActor.GetMarketsRegionIdHistory(region.id, item.id)
          }
      }

    case histories: List[TradeHistory] => processResult(histories)

    case RegionActor.GetAllResult(_regions) =>
      context.become(inProcess(items, _regions))
      if (items.nonEmpty && regions.nonEmpty) self ! Next

    case ItemActor.GetAllResult(_items) =>
      context.become(inProcess(_items, regions))
      if (items.nonEmpty && regions.nonEmpty) self ! Next
  }

  def idle: Actor.Receive = {
    case Update =>
      context.become(inProcess())
      regionActor ! RegionActor.GetAll
      itemsActor ! ItemActor.GetOnId

    case histories: List[TradeHistory] => processResult(histories)

  }

  private def processResult(histories: List[TradeHistory]): Unit = {
    histories.foreach { tradeHistoryActor ! TradeHistoryActor.WriteOrUpdate(_) }
  }

  override def receive: Actor.Receive = idle

  private case object Next
}

object TradeHistoryLoader {
  case object Update
}
