package info.golushkov.eve.tool.akka.actors.loaders

import akka.actor.{Actor, ActorLogging, ActorRef}
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.mongo.ItemActor
import info.golushkov.eve.tool.akka.models.Item

class ItemLoader(itemActor: ActorRef, api: ActorRef) extends Actor with ActorLogging{
  import ItemLoader._

  override def receive = {
    case Update =>
      api ! ApiActor.GetUniverseTypesTypeId

    case ids: List[Int] =>
      log.info(s"processing... queue size = ${ids.size}")
      ids match {
        case id :: tail =>
          api ! ApiActor.GetUniverseTypesTypeId(id)

        case Nil => ()
      }

    case item: Item =>
      itemActor ! ItemActor.WriteOrUpdate(item)
  }
}

object ItemLoader {
  case object Update
}
