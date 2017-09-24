package info.golushkov.eve.tool.akka.actors.loaders

import akka.actor.{Actor, ActorLogging, ActorRef}
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.mongo.ItemActor
import info.golushkov.eve.tool.akka.models.Item

class ItemLoader(itemActor: ActorRef, api: ActorRef) extends Actor with ActorLogging{
  import ItemLoader._
  private var itemIds: List[Int] = Nil

  override def receive = {
    case Update =>
      log.info(s"Update - start!")
      api ! ApiActor.GetUniverseTypesTypeId

    case ids: List[Int] =>
      log.info(s"processing... queue size = ${ids.size}")
      this.itemIds = ids
      self ! Next

    case Next =>
      log.info(s"next item")
      itemIds match {
        case id :: tail =>
          itemIds = tail
          api ! ApiActor.GetUniverseTypesTypeId(id)

        case Nil => ()
      }

    case item: Item =>
      log.info(s"load item!")
      itemActor ! ItemActor.WriteOrUpdate(item)
      self ! Next
  }

  private case object Next
}

object ItemLoader {
  case object Update
}
