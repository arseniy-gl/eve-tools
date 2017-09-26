package info.golushkov.eve.tool.akka.actors.loaders

import akka.actor.{Actor, ActorLogging, ActorRef}
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.mongo.ItemActor
import info.golushkov.eve.tool.akka.models.Item

class ItemLoader(itemActor: ActorRef, api: ActorRef) extends Actor with ActorLogging{
  import ItemLoader._
  private var itemIds: List[Int] = Nil //TODO перевести в контекст

  override def receive = {
    case Update =>
      log.info(s"Update - start!")
      api ! ApiActor.GetUniverseTypes()

    case ids: List[Int] =>
      this.itemIds ++= ids
      self ! Next


    case Next =>
      itemIds match {
        case id :: tail =>
          itemIds = tail
          api ! ApiActor.GetUniverseTypesTypeId(id)

        case Nil => ()
      }

    case item: Item =>
      log.info(s"load item [tail = ${itemIds.size} ]")
      itemActor ! ItemActor.WriteOrUpdate(item)
      self ! Next
  }

  private case object Next
}

object ItemLoader {
  case object Update
}
