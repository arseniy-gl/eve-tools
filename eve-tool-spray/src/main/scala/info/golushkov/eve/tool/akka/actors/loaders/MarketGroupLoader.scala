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

    case ids: List[_] =>
      this.marketGroupIds = ids.collect { case id: Int => id }
      self ! Next

    case Next =>
      marketGroupIds match {
        case id :: tail =>
          marketGroupIds = tail
          api ! ApiActor.GetMarketsGroupsMarketGroupId(id)

        case Nil => ()
      }

    case mg: MarketGroup =>
      log.info(s"load market group [tail = ${marketGroupIds.size} ]")
      marketGroupActor ! MarketGroupActor.WriteOrUpdate(mg)
      self ! Next
  }

  private case object Next
}

object MarketGroupLoader {
  case object Update
}
