package info.golushkov.eve.tool.akka.actors.loaders

import akka.actor.{Actor, ActorLogging, ActorRef}
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.mongo.MarketGroupActor
import info.golushkov.eve.tool.akka.models.MarketGroup

class MarketGroupLoader(marketGroupActor: ActorRef, api: ActorRef) extends Actor with ActorLogging {
  import MarketGroupLoader._

  override def receive: Actor.Receive = {
    case Update =>
      log.info(s"Update - start!")
      api ! ApiActor.GetMarketsGroups

    case ids: List[Int] =>
      log.info(s"processing... queue size = ${ids.size}")
      ids match {
        case id :: tail =>
          api ! ApiActor.GetMarketsGroupsMarketGroupId(id)
          self ! tail

        case Nil => ()
      }
    case mg: MarketGroup =>
      marketGroupActor ! MarketGroupActor.WriteOrUpdate(mg)
  }
}

object MarketGroupLoader {
  case object Update
}
