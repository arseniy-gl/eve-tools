package info.golushkov.eve.tool.akka.actors.loaders

import akka.actor.{Actor, ActorLogging, ActorRef}
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.mongo.PriceActor
import info.golushkov.eve.tool.akka.models.Price

class PriceLoader(priceActor: ActorRef, api: ActorRef) extends Actor with ActorLogging {
  import PriceLoader._

  override def receive: PartialFunction[Any, Unit] = {
    case Update =>
      log.info(s"Update - start!")
      api ! ApiActor.GetMarketsPrices

    case prices:List[Price] =>
      println("PriceLoader::prices:List[Price]")
      prices.foreach({ price =>
        priceActor ! PriceActor.WriteOrUpdate(price)
      })
  }
}

object PriceLoader {
  case object Update
}
