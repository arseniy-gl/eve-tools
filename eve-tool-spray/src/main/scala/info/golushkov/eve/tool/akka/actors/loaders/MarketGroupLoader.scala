package info.golushkov.eve.tool.akka.actors.loaders

import akka.actor.{Actor, ActorLogging, ActorRef}
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.mongo.MarketGroupActor
import info.golushkov.eve.tool.akka.models.MarketGroup

class MarketGroupLoader(marketGroupActor: ActorRef, api: ActorRef) extends Actor with ActorLogging {
  import MarketGroupLoader._
  private var marketGroupIds: List[Int] = Nil

  override def receive: Actor.Receive = {
    case Update =>
      log.info(s"Update - start!")
      api ! ApiActor.GetMarketsGroups

    case ids: List[Int] =>
      log.info(s"processing... queue size = ${ids.size}")
      this.marketGroupIds = ids
      self ! Next

    case Next =>
      log.info(s"Next load market group")
      marketGroupIds match {
        case id :: tail =>
          marketGroupIds = tail
          api ! ApiActor.GetMarketsGroupsMarketGroupId(id)

        case Nil => ()
      }

    case mg: MarketGroup =>
      log.info(s"load market group")
      marketGroupActor ! MarketGroupActor.WriteOrUpdate(mg)
      self ! Next
  }

  private case object Next
}

object MarketGroupLoader {
  case object Update
}
