package info.golushkov.eve.tool.akka.actors.loaders

import akka.actor.{Actor, ActorLogging, ActorRef}
import info.golushkov.eve.tool.akka.actors.ApiActor
import info.golushkov.eve.tool.akka.actors.mongo.ItemActor
import info.golushkov.eve.tool.akka.models.Item

class ItemLoader(itemActor: ActorRef, api: ActorRef) extends Actor with ActorLogging{
  import ItemLoader._

  def idle: Actor.Receive = {
    case Update =>
      log.info(s"Update - start!")
      api ! ApiActor.GetUniverseTypes()

    case item: Item =>
      log.info(s"load item [tail = 0 ]")
      itemActor ! ItemActor.WriteOrUpdate(item)
      self ! Next
  }

  def inProcess(queue: List[Task] = Nil, itemIds: List[Int] = Nil): Actor.Receive = {
    case Update =>
      context.become(inProcess(queue :+ Task(Update, sender())))

    case ids: List[Int] =>
      context.become(inProcess(queue, itemIds ::: ids))
      self ! Next

    case Next =>
      itemIds match {
        case id :: tail =>
          context.become(inProcess(queue, tail))
          api ! ApiActor.GetUniverseTypesTypeId(id)

        case Nil =>
          queue.filterNot(_.msg == Update) match {
            case Nil => context.become(idle)

            case next::tail =>
              context.become(inProcess(tail))
              self ! next
          }

      }

    case item: Item =>
      log.info(s"load item [tail = ${itemIds.size} ]")
      itemActor ! ItemActor.WriteOrUpdate(item)
      self ! Next
  }

  override def receive: Actor.Receive = {
    case Update =>
      log.info(s"Update - start!")
      api ! ApiActor.GetUniverseTypes()
  }

  private case class Task(msg: AnyRef, sender: ActorRef)
  private case object Next
}

object ItemLoader {
  case object Update
}
